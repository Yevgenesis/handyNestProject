package codezilla.handynestproject.security.service.impl;

import codezilla.handynestproject.security.model.JwtAuthenticationResponse;
import codezilla.handynestproject.security.model.SignInRequest;
import codezilla.handynestproject.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userService;
    private final JwtService jwtService;


    @Override
    public JwtAuthenticationResponse authenticate(SignInRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(),
                        request.getPassword()));

        UserDetails user = userService.loadUserByUsername(request.getLogin());

        String token = jwtService.generateToken(user);

        return new JwtAuthenticationResponse(token);
    }
}
