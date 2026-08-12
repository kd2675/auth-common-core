package auth.common.core.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class VerifiedJwtPrincipalFilterTest {

    private static final String SECRET =
            "auth-test-jwt-secret-hs512-minimum-length-64-chars-1234567890-extra";
    private final VerifiedJwtPrincipalFilter filter = new VerifiedJwtPrincipalFilter(
            SECRET,
            "http://localhost:9000",
            "semo-api,muse-api",
            new ObjectMapper()
    );

    @Test
    void doFilter_validToken_exposesVerifiedUserKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token("semo-api", "api", "user-key"));
        request.addHeader("X-User-Key", "forged-user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(request.getAttribute(VerifiedJwtPrincipalFilter.USER_KEY_ATTRIBUTE)).isEqualTo("user-key");
    }

    @Test
    void doFilter_missingToken_returnsUnauthorized() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilter_secondAllowedAudience_exposesVerifiedUserKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token("muse-api", "api", "muse-user-key"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(request.getAttribute(VerifiedJwtPrincipalFilter.USER_KEY_ATTRIBUTE)).isEqualTo("muse-user-key");
    }

    @Test
    void doFilter_wrongAudience_returnsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token("stock-api", "api", "user-key"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    private String token(String audience, String scope, String userKey) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .issuer("http://localhost:9000")
                .subject("test-user")
                .audience().add(audience).and()
                .issuedAt(new Date(now))
                .expiration(new Date(now + 60_000))
                .claim("scope", scope)
                .claim("userKey", userKey)
                .claim("role", "USER")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512)
                .compact();
    }
}
