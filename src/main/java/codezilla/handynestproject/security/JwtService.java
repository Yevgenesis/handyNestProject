package codezilla.handynestproject.security;

import codezilla.handynestproject.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretSigningKey;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${jwttoken.signing.key}") String jwttokenSigningKey,
            @Value("${app.security.jwt.access-token-ttl:15m}") Duration accessTokenTtl
    ) {
        this.secretSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwttokenSigningKey));
        this.accessTokenTtl = accessTokenTtl;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList()));

        if (userDetails instanceof User userEntity) {
            claims.put("userId", userEntity.getId());
            claims.put("publicId", userEntity.getPublicId());
            claims.put("login", userEntity.getEmail());
        }

        return generateToken(claims, userDetails.getUsername());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("publicId", user.getPublicId());
        claims.put("login", user.getEmail());
        claims.put("roles", user.getRoles().stream().map(Enum::name).sorted().toList());

        return generateToken(claims, user.getEmail());
    }

    private String generateToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .claims()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenTtl.toMillis()))
                .subject(subject)
                .add(extraClaims)
                .and()
                .signWith(secretSigningKey)
                .compact();
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
