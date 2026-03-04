package auth.common.core.constant;

import java.util.Locale;
import java.util.Set;

/**
 * ZeroQ 공통 사용자 역할 상수
 *
 * Canonical roles:
 * - USER: 일반 사용자(zeroq-front-service)
 * - MANAGER: 매장 중간관리자(zeroq-front-admin)
 * - ADMIN: 플랫폼 운영자
 */
public final class UserRole {

    public static final String USER = "USER";
    public static final String MANAGER = "MANAGER";
    public static final String ADMIN = "ADMIN";

    private static final Set<String> VALID_ROLES = Set.of(
            USER,
            MANAGER,
            ADMIN
    );

    private UserRole() {
    }

    public static String normalize(String role) {
        if (role == null || role.trim().isEmpty()) {
            return USER;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        if (!VALID_ROLES.contains(normalized)) {
            return USER;
        }
        return normalized;
    }

    public static boolean isAdmin(String role) {
        String normalized = normalize(role);
        return ADMIN.equals(normalized);
    }

    public static boolean isManager(String role) {
        String normalized = normalize(role);
        return MANAGER.equals(normalized);
    }

    public static boolean isUser(String role) {
        String normalized = normalize(role);
        return USER.equals(normalized);
    }

    public static boolean isValidRole(String role) {
        String normalized = normalize(role);
        return USER.equals(normalized)
                || MANAGER.equals(normalized)
                || ADMIN.equals(normalized);
    }
}
