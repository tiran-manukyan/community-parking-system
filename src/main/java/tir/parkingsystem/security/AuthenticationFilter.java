package tir.parkingsystem.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    @Value("${auth.third-party-system.gate.api-key-header}")
    private String apiKeyHeader;

    @Value("${auth.third-party-system.gate.secret-key}")
    private String secretApiKey;

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException {
        String apiKey = request.getHeader(apiKeyHeader);
        if (apiKey != null && apiKey.equals(secretApiKey)) {
            List<SimpleGrantedAuthority> roleGate = List.of(new SimpleGrantedAuthority("ROLE_GATE"));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("third-party-system-gate", null, roleGate));
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = getJwtFromRequest(request);
            if (Objects.nonNull(token) && jwtUtil.validateToken(token)) {
                Authentication authentication = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException e) {
            response.getWriter().write(e.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/user/login".equalsIgnoreCase(path) || "/user/register".equalsIgnoreCase(path);
    }

    @PostConstruct
    public void validateValues() {
        if (apiKeyHeader == null || secretApiKey == null) {
            throw new IllegalStateException("API Key Header or Secret Key is not configured properly.");
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
