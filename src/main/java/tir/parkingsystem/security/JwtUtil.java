package tir.parkingsystem.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import tir.parkingsystem.exception.AuthorizationException;
import tir.parkingsystem.security.model.CustomUserDetails;
import tir.parkingsystem.service.user.UserDetailsServiceImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${auth.jwt.secret-key}")
    private String secretKey;

    @Value("${auth.jwt.expire-duration}")
    private Duration expireDuration;

    private final UserDetailsServiceImpl userDetailsService;

    public String generateToken(Authentication authentication) {
        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(Instant.now().plus(expireDuration).toEpochMilli()))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            throw new AuthorizationException("JWT token is expired!");
        } catch (SignatureException | MalformedJwtException e) {
            throw new AuthorizationException("JWT token is invalid!");
        } catch (RuntimeException e) {
            throw new AuthorizationException(e.getMessage());
        }
    }

    public Authentication getAuthentication(String token) {
        String username = extractUsername(token);
        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
