package auth.common.core.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * UserContext ArgumentResolver
 *
 * Gateway가 보낸 X-User-Key, X-User-Name, X-User-Role 헤더를
 * UserContext 객체로 변환하여 컨트롤러에서 직접 사용할 수 있게 함
 *
 * 사용 예시:
 * @GetMapping("/my-info")
 * public ResponseEntity<?> getMyInfo(UserContext userContext) {
 *     if (!userContext.isAuthenticated()) {
 *         throw new AuthException("Login required");
 *     }
 *     String userKey = userContext.getUserKey();
 *     // ...
 * }
 *
 * 등록 방법 (각 서비스의 WebMvcConfigurer에서):
 * @Override
 * public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
 *     resolvers.add(new UserContextArgumentResolver());
 * }
 *
 * 주의: 라이브러리이므로 @Component를 사용하지 않음
 * 각 서비스에서 직접 생성하여 등록해야 함
 */
public class UserContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(UserContext.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return UserContext.builder().build();
        }

        String userKeyHeader = request.getHeader("X-User-Key");
        String userNameHeader = decodeUserName(request.getHeader("X-User-Name"));
        String userRoleHeader = request.getHeader("X-User-Role");

        return UserContext.builder()
                .userKey(emptyToNull(userKeyHeader))
                .userName(userNameHeader)
                .role(userRoleHeader)
                .build();
    }

    private String decodeUserName(String userNameHeader) {
        if (userNameHeader == null || userNameHeader.isEmpty()) {
            return userNameHeader;
        }
        try {
            return URLDecoder.decode(userNameHeader, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return userNameHeader;
        }
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
