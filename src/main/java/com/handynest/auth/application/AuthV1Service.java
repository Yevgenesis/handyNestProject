package com.handynest.auth.application;

import com.handynest.identity.User;
import com.handynest.identity.RoleName;
import com.handynest.identity.UserRepository;
import com.handynest.auth.security.JwtService;
import com.handynest.auth.api.AuthLoginRequest;
import com.handynest.auth.api.AuthRegisterRequest;
import com.handynest.auth.api.AuthTokenResponse;
import com.handynest.auth.api.AuthUserResponse;
import com.handynest.auth.session.IssuedRefreshToken;
import com.handynest.auth.session.RefreshTokenService;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.UnauthorizedBusinessException;
import com.handynest.common.publicid.PublicIdGenerator;
import com.handynest.common.ratelimit.RateLimitService;
import com.handynest.identity.AccountType;
import com.handynest.identity.UserAccountState;
import com.handynest.identity.CustomerProfile;
import com.handynest.identity.CustomerProfileRepository;
import com.handynest.identity.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthV1Service {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitService rateLimitService;
    private final CustomerProfileRepository customerProfileRepository;

    @Transactional
    public AuthTokenResponse register(AuthRegisterRequest request, HttpServletRequest servletRequest) {
        rateLimitService.consumeAuthRegister(servletRequest);

        String email = normalizeEmail(request.email());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("User with this email already exists");
        }

        User user = new User();
        user.setPublicId(PublicIdGenerator.defaultGenerator().newUlid());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(email);
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDeleted(false);
        user.setPhoneVerified(false);
        user.setStatus(UserStatus.ACTIVE);
        user.setAccountType(AccountType.PERSONAL);
        user.setRoles(new HashSet<>(Set.of(RoleName.USER)));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("User with this email already exists");
        }
        customerProfileRepository.save(new CustomerProfile(user));

        return issueTokenResponse(user, servletRequest);
    }

    @Transactional
    public AuthTokenResponse login(AuthLoginRequest request, HttpServletRequest servletRequest) {
        String email = normalizeEmail(request.email());
        rateLimitService.consumeAuthLogin(email, servletRequest);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (BadCredentialsException exception) {
            throw new UnauthorizedBusinessException("Invalid email or password");
        } catch (AuthenticationException exception) {
            throw new UnauthorizedBusinessException("Authentication failed");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedBusinessException("Invalid email or password"));
        if (!UserAccountState.isSessionAllowed(user)) {
            throw new UnauthorizedBusinessException("Invalid email or password");
        }

        return issueTokenResponse(user, servletRequest);
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshToken, HttpServletRequest servletRequest) {
        rateLimitService.consumeAuthRefresh(servletRequest);

        IssuedRefreshToken issuedRefreshToken = refreshTokenService.rotate(
                refreshToken,
                servletRequest
        );
        User user = issuedRefreshToken.refreshToken().getUser();

        return tokenResponse(user, issuedRefreshToken.rawToken());
    }

    @Transactional
    public void logout(String refreshToken, HttpServletRequest servletRequest) {
        refreshTokenService.revoke(refreshToken, servletRequest);
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedBusinessException("Authentication required");
        }

        User user = userRepository.findByEmail(normalizeEmail(userDetails.getUsername()))
                .orElseThrow(() -> new UnauthorizedBusinessException("Authentication required"));
        if (!UserAccountState.isSessionAllowed(user)) {
            throw new UnauthorizedBusinessException("Authentication required");
        }

        return userResponse(user);
    }

    private AuthTokenResponse issueTokenResponse(User user, HttpServletRequest servletRequest) {
        IssuedRefreshToken issuedRefreshToken = refreshTokenService.issue(user, servletRequest);
        return tokenResponse(user, issuedRefreshToken.rawToken());
    }

    private AuthTokenResponse tokenResponse(User user, String rawRefreshToken) {
        return new AuthTokenResponse(
                jwtService.generateToken(user),
                rawRefreshToken,
                TOKEN_TYPE,
                jwtService.getAccessTokenTtl().toSeconds(),
                userResponse(user)
        );
    }

    private AuthUserResponse userResponse(User user) {
        return new AuthUserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEmailVerified(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .collect(TreeSet::new, TreeSet::add, TreeSet::addAll)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
