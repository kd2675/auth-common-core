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
 * Gateway가 보낸 X-User-Id, X-User-Name, X-User-Role 헤더를
 * UserContext 객체로 변환하여 컨트롤러에서 직접 사용할 수 있게 함
 *
 * 사용 예시:
 * @GetMapping("/my-info")
 * public ResponseEntity<?> getMyInfo(UserContext userContext) {
 *     if (!userContext.isAuthenticated()) {
 *         throw new AuthException("Login required");
 *     }
 *     Long userId = userContext.getUserId();
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

        String userIdHeader = request.getHeader("X-User-Id");
        String userNameHeader = decodeUserName(request.getHeader("X-User-Name"));
        String userRoleHeader = request.getHeader("X-User-Role");

        Long userId = parseUserId(userIdHeader);

        return UserContext.builder()
                .userId(userId)
                .userName(userNameHeader)
                .role(userRoleHeader)
                .build();
    }

    private Long parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return null;
        }
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
}
