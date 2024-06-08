package codezilla.handynestproject.security;

import codezilla.handynestproject.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey secretSigningKey;

    //Reading the signing key from the get token.signing property file.key encoded in Base64
    public JwtService(@Value("${jwttoken.signing.key}") String jwttokenSigningKey) {
        this.secretSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwttokenSigningKey));
    }

    // Token generation
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof User userEntity) {
            claims.put("userId", userEntity);
            claims.put("login", userEntity.getEmail());
             claims.put("role", userDetails.getAuthorities());
        }
        return generateToken(claims, userDetails);
    }

    // The method directly generates a token based on a set of user data
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 100000 * 60 * 24))
                .subject(userDetails.getUsername())
                .add(extraClaims)
                .and()
                .signWith(secretSigningKey) // resume JwtBuilder calls
                .compact();
    }

    // Retrieve a username from a token
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Checking that the token is valid, it is for this user that the validity period has not expired
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Retrieve data from the @param claimsResolvers token data extraction function
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    // Checking the token for expiration @return true if the token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Retrieve the expiration date of the token @return expiration date
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Retrieve all data from token
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .setSigningKey(secretSigningKey)
                .build().parseSignedClaims(token).getPayload();
    }
}