package com.handynest.auth.application;

import com.handynest.auth.api.AuthLoginRequest;
import com.handynest.auth.api.AuthRegisterRequest;
import com.handynest.auth.api.AuthTokenResponse;
import com.handynest.auth.api.AuthUserResponse;
import com.handynest.auth.api.EmailVerificationRequestResponse;
import com.handynest.auth.api.EmailVerificationVerifyRequest;
import com.handynest.auth.api.EmailVerificationVerifyResponse;
import com.handynest.auth.api.PhoneOtpRequestResponse;
import com.handynest.auth.api.PhoneOtpVerifyRequest;
import com.handynest.auth.api.PhoneOtpVerifyResponse;
import com.handynest.auth.security.JwtService;
import com.handynest.auth.session.IssuedRefreshToken;
import com.handynest.auth.session.RefreshTokenService;
import com.handynest.auth.verification.EmailVerificationService;
import com.handynest.auth.verification.OtpService;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.UnauthorizedBusinessException;
import com.handynest.common.publicid.PublicIdGenerator;
import com.handynest.common.ratelimit.RateLimitService;
import com.handynest.identity.AccountType;
import com.handynest.identity.CustomerProfile;
import com.handynest.identity.CustomerProfileRepository;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserAccountState;
import com.handynest.identity.UserConsentService;
import com.handynest.identity.UserRepository;
import com.handynest.identity.UserStatus;
import com.handynest.notification.DomainEventPublisher;
import com.handynest.notification.DomainEventType;
import com.handynest.notification.NotificationType;
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
  private final OtpService otpService;
  private final EmailVerificationService emailVerificationService;
  private final UserConsentService userConsentService;
  private final DomainEventPublisher domainEventPublisher;

  @Transactional
  public AuthTokenResponse register(
      AuthRegisterRequest request, HttpServletRequest servletRequest) {
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
    userConsentService.acceptRegistrationConsents(user, request.consents(), servletRequest);
    domainEventPublisher.publish(
        DomainEventType.USER_REGISTERED,
        "User",
        user.getPublicId(),
        user,
        NotificationType.USER_REGISTERED,
        "Добро пожаловать в HandyNest",
        "Аккаунт успешно создан",
        "User",
        user.getPublicId(),
        java.util.Map.of());

    return issueTokenResponse(user, servletRequest);
  }

  @Transactional
  public AuthTokenResponse login(AuthLoginRequest request, HttpServletRequest servletRequest) {
    String email = normalizeEmail(request.email());
    rateLimitService.consumeAuthLogin(email, servletRequest);

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, request.password()));
    } catch (BadCredentialsException exception) {
      throw new UnauthorizedBusinessException("Invalid email or password");
    } catch (AuthenticationException exception) {
      throw new UnauthorizedBusinessException("Authentication failed");
    }

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UnauthorizedBusinessException("Invalid email or password"));
    if (!UserAccountState.isSessionAllowed(user)) {
      throw new UnauthorizedBusinessException("Invalid email or password");
    }

    return issueTokenResponse(user, servletRequest);
  }

  @Transactional
  public AuthTokenResponse refresh(String refreshToken, HttpServletRequest servletRequest) {
    rateLimitService.consumeAuthRefresh(servletRequest);

    IssuedRefreshToken issuedRefreshToken =
        refreshTokenService.rotate(refreshToken, servletRequest);
    User user = issuedRefreshToken.refreshToken().getUser();

    return tokenResponse(user, issuedRefreshToken.rawToken());
  }

  @Transactional
  public void logout(String refreshToken, HttpServletRequest servletRequest) {
    refreshTokenService.revoke(refreshToken, servletRequest);
  }

  @Transactional(readOnly = true)
  public AuthUserResponse me(UserDetails userDetails) {
    return userResponse(currentUser(userDetails));
  }

  @Transactional
  public PhoneOtpRequestResponse requestPhoneOtp(
      UserDetails userDetails, HttpServletRequest servletRequest) {
    User user = currentUser(userDetails);
    String phone = requirePhone(user);
    rateLimitService.consumePhoneOtpRequest(phone, servletRequest);
    return otpService.requestPhoneOtp(user, servletRequest);
  }

  @Transactional(noRollbackFor = BadRequestBusinessException.class)
  public PhoneOtpVerifyResponse verifyPhoneOtp(
      UserDetails userDetails, PhoneOtpVerifyRequest request, HttpServletRequest servletRequest) {
    User user = currentUser(userDetails);
    String phone = requirePhone(user);
    rateLimitService.consumePhoneOtpVerify(phone, servletRequest);
    return otpService.verifyPhoneOtp(user, request.code());
  }

  @Transactional
  public EmailVerificationRequestResponse requestEmailVerification(
      UserDetails userDetails, HttpServletRequest servletRequest) {
    User user = currentUser(userDetails);
    rateLimitService.consumeEmailVerificationRequest(user.getEmail(), servletRequest);
    return emailVerificationService.requestVerification(user, servletRequest);
  }

  @Transactional
  public EmailVerificationVerifyResponse verifyEmail(EmailVerificationVerifyRequest request) {
    return emailVerificationService.verify(request.token());
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
        userResponse(user));
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
            .collect(TreeSet::new, TreeSet::add, TreeSet::addAll));
  }

  private User currentUser(UserDetails userDetails) {
    if (userDetails == null) {
      throw new UnauthorizedBusinessException("Authentication required");
    }

    User user =
        userRepository
            .findByEmail(normalizeEmail(userDetails.getUsername()))
            .orElseThrow(() -> new UnauthorizedBusinessException("Authentication required"));
    if (!UserAccountState.isSessionAllowed(user)) {
      throw new UnauthorizedBusinessException("Authentication required");
    }
    return user;
  }

  private String requirePhone(User user) {
    if (user.getPhone() == null || user.getPhone().isBlank()) {
      throw new BadRequestBusinessException("Phone number is required");
    }
    return user.getPhone().trim();
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
