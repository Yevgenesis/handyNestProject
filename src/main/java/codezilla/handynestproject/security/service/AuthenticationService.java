package codezilla.handynestproject.security.service;


import codezilla.handynestproject.security.model.JwtAuthenticationResponse;
import codezilla.handynestproject.security.model.SignInRequest;

public interface AuthenticationService {

    JwtAuthenticationResponse authenticate(SignInRequest request);
}
