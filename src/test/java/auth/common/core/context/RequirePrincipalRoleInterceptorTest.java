package auth.common.core.context;

import auth.common.core.constant.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequirePrincipalRoleInterceptorTest {

    private final RequirePrincipalRoleInterceptor interceptor = new RequirePrincipalRoleInterceptor();

    @Test
    void preHandle_userRoleWithUserKey_returnsTrue() throws Exception {
        MockHttpServletRequest request = request("user-key", "ROLE_USER");

        boolean result = interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                handlerMethod("userOnly")
        );

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_gatewayRoleForUserOnlyMethod_throwsForbidden() throws Exception {
        MockHttpServletRequest request = request("gateway-key", "ROLE_GATEWAY");

        assertThatThrownBy(() -> interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                handlerMethod("userOnly")
        ))
                .isInstanceOfSatisfying(GeneralException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(Code.FORBIDDEN)
                );
    }

    @Test
    void preHandle_missingUserKey_throwsUnauthorized() throws Exception {
        MockHttpServletRequest request = request(null, UserRole.USER);

        assertThatThrownBy(() -> interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                handlerMethod("userOnly")
        ))
                .isInstanceOfSatisfying(GeneralException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(Code.UNAUTHORIZED)
                );
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

    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(controller, method);
    }

    private static class TestController {

        @RequirePrincipalRole
        void userOnly() {
        }
    }
}
