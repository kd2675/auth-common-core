package auth.common.core.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import web.common.core.response.base.dto.ResponseErrorDTO;
import web.common.core.response.base.vo.Code;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

/**
 * Verifies a Bearer access token for endpoints that are called directly
 * instead of through cloud-back-server.
 */
public class VerifiedJwtPrincipalFilter extends OncePerRequestFilter {

    public static final String USER_KEY_ATTRIBUTE =
            VerifiedJwtPrincipalFilter.class.getName() + ".userKey";
    public static final String USER_NAME_ATTRIBUTE =
            VerifiedJwtPrincipalFilter.class.getName() + ".userName";
    public static final String USER_ROLE_ATTRIBUTE =
            VerifiedJwtPrincipalFilter.class.getName() + ".userRole";

    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey key;
    private final String issuer;
    private final String audience;
    private final ObjectMapper objectMapper;

    public VerifiedJwtPrincipalFilter(
            String secret,
            String issuer,
            String audience,
            ObjectMapper objectMapper
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)
                || authorization.length() == BEARER_PREFIX.length()) {
            writeUnauthorized(response, "Bearer access token is required");
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(authorization.substring(BEARER_PREFIX.length()))
                    .getPayload();
            validateAudience(claims.get("aud"));
            validateScope(claims.get("scope"));

            setStringAttribute(request, USER_KEY_ATTRIBUTE, claims.get("userKey"));
            setStringAttribute(request, USER_NAME_ATTRIBUTE, claims.getSubject());
            setStringAttribute(request, USER_ROLE_ATTRIBUTE, claims.get("role"));
            if (request.getAttribute(USER_KEY_ATTRIBUTE) == null) {
                writeUnauthorized(response, "Access token user key is missing");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            writeUnauthorized(response, "Invalid or expired access token");
        }
    }

    private void validateAudience(Object claim) {
        boolean matches = claim instanceof String value && audience.equals(value);
        if (claim instanceof Collection<?> values) {
            matches = values.stream().anyMatch(audience::equals);
        }
        if (!matches) {
            throw new IllegalArgumentException("Unexpected token audience");
        }
    }

    private void validateScope(Object claim) {
        if (!(claim instanceof String scope)
                || Arrays.stream(scope.split(" ")).noneMatch("api"::equals)) {
            throw new IllegalArgumentException("Required API scope is missing");
        }
    }

    private void setStringAttribute(HttpServletRequest request, String name, Object value) {
        if (value instanceof String text && !text.isBlank()) {
            request.setAttribute(name, text);
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(Code.UNAUTHORIZED.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ResponseErrorDTO.of(Code.UNAUTHORIZED, message));
    }
}
