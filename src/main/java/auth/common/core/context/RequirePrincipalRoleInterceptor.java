package auth.common.core.context;

import auth.common.core.constant.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class RequirePrincipalRoleInterceptor implements HandlerInterceptor {
    private static final String USER_KEY_HEADER = "X-User-Key";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequirePrincipalRole requirement = findRequirement(handlerMethod);
        if (requirement == null) {
            return true;
        }

        String userKey = request.getHeader(USER_KEY_HEADER);
        if (requirement.requireUserKey() && !hasText(userKey)) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }

        String role = request.getHeader(USER_ROLE_HEADER);
        if (!UserRole.hasAnyRole(role, requirement.anyOf())) {
            throw new GeneralException(
                    Code.FORBIDDEN,
                    "Required role: " + formatRequiredRoles(requirement.anyOf())
            );
        }

        return true;
    }

    private RequirePrincipalRole findRequirement(HandlerMethod handlerMethod) {
        RequirePrincipalRole methodRequirement = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(),
                RequirePrincipalRole.class
        );
        if (methodRequirement != null) {
            return methodRequirement;
        }

        return AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(),
                RequirePrincipalRole.class
        );
    }

    private String formatRequiredRoles(String[] roles) {
        if (roles == null || roles.length == 0) {
            return "none";
        }
        return Arrays.stream(roles)
                .map(UserRole::canonicalize)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
