package auth.common.core.context;

import auth.common.core.constant.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequirePrincipalRoleFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
    private final RequirePrincipalRoleFilter filter = new RequirePrincipalRoleFilter(handlerMapping, objectMapper);

    @Test
    void doFilter_userRoleWithUserKey_continuesChain() throws Exception {
        MockHttpServletRequest request = request("user-key", "ROLE_USER");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain("userOnly"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void doFilter_adminRoleForUserOnlyMethod_continuesChain() throws Exception {
        MockHttpServletRequest request = request("admin-key", "ROLE_ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain("userOnly"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void doFilter_gatewayRoleForUserOnlyMethod_returnsForbidden() throws Exception {
        MockHttpServletRequest request = request("gateway-key", "ROLE_GATEWAY");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain("userOnly"));

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        JsonNode body = readBody(response);
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asInt()).isEqualTo(4030000);
        assertThat(body.get("message").asText()).isEqualTo("Required role: USER");
    }

    @Test
    void doFilter_missingUserKey_returnsUnauthorized() throws Exception {
        MockHttpServletRequest request = request(null, UserRole.USER);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain("userOnly"));

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        JsonNode body = readBody(response);
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asInt()).isEqualTo(4010200);
        assertThat(body.get("message").asText()).isEqualTo("Login required");
    }

    @Test
    void doFilter_userRoleForAdminOnlyMethod_returnsForbidden() throws Exception {
        MockHttpServletRequest request = request("user-key", "ROLE_USER");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(handlerMapping.getHandler(request)).thenReturn(handlerExecutionChain("adminOnly"));

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        JsonNode body = readBody(response);
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asInt()).isEqualTo(4030000);
        assertThat(body.get("message").asText()).isEqualTo("Required role: ADMIN");
    }

    @Test
    void doFilter_wrongHttpMethod_delegatesToDispatcherWithoutWrappingAsServletException() throws Exception {
        MockHttpServletRequest request = request("user-key", UserRole.USER);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(handlerMapping.getHandler(request)).thenThrow(
                new HttpRequestMethodNotSupportedException("POST", java.util.List.of("GET"))
        );

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }

    private MockHttpServletRequest request(String userKey, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (userKey != null) {
            request.addHeader("X-User-Key", userKey);
        }
        if (role != null) {
            request.addHeader("X-User-Role", role);
        }
        return request;
    }

    private HandlerExecutionChain handlerExecutionChain(String methodName) throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new HandlerExecutionChain(new HandlerMethod(controller, method));
    }

    private JsonNode readBody(MockHttpServletResponse response) throws Exception {
        return objectMapper.readTree(response.getContentAsString());
    }

    private static class TestController {

        @RequirePrincipalRole
        void userOnly() {
        }

        @RequirePrincipalRole(anyOf = {UserRole.ADMIN})
        void adminOnly() {
        }
    }
}
