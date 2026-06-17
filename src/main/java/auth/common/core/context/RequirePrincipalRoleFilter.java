package auth.common.core.context;

import auth.common.core.constant.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import web.common.core.response.base.vo.Code;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class RequirePrincipalRoleFilter extends OncePerRequestFilter {
    private static final String USER_KEY_HEADER = "X-User-Key";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final RequestMappingHandlerMapping handlerMapping;

    public RequirePrincipalRoleFilter(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        RequirePrincipalRole requirement = findRequirement(request);
        if (requirement == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String userKey = request.getHeader(USER_KEY_HEADER);
        if (requirement.requireUserKey() && !hasText(userKey)) {
            writeError(response, Code.UNAUTHORIZED, "Login required");
            return;
        }

        String role = request.getHeader(USER_ROLE_HEADER);
        if (!UserRole.hasAnyRole(role, requirement.anyOf())) {
            writeError(response, Code.FORBIDDEN, "Required role: " + formatRequiredRoles(requirement.anyOf()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RequirePrincipalRole findRequirement(HttpServletRequest request) throws ServletException {
        HandlerExecutionChain executionChain;
        try {
            executionChain = handlerMapping.getHandler(request);
        } catch (Exception ex) {
            throw new ServletException("Failed to resolve handler for role requirement", ex);
        }

        if (executionChain == null || !(executionChain.getHandler() instanceof HandlerMethod handlerMethod)) {
            return null;
        }

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

    private void writeError(HttpServletResponse response, Code code, String message) throws IOException {
        response.setStatus(code.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format(
                "{\"success\":false,\"code\":%d,\"message\":\"%s\"}",
                code.getCode(),
                escapeJson(message)
        ));
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

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
