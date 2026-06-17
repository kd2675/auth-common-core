package auth.common.core.constant;

import java.util.Locale;
import java.util.Set;

/**
 * ZeroQ 공통 인증 주체 역할 상수
 *
 * Account roles:
 * - USER: 일반 사용자(zeroq-front-service)
 * - MANAGER: 매장 중간관리자(zeroq-front-admin)
 * - ADMIN: 플랫폼 운영자
 *
 * Service principal roles:
 * - GATEWAY: 인증된 센서 게이트웨이 또는 게이트웨이 서비스 주체
 */
public final class UserRole {

    public static final String USER = "USER";
    public static final String MANAGER = "MANAGER";
    public static final String ADMIN = "ADMIN";
    public static final String GATEWAY = "GATEWAY";

    private static final Set<String> ACCOUNT_ROLES = Set.of(
            USER,
            MANAGER,
            ADMIN
    );

    private static final Set<String> PRINCIPAL_ROLES = Set.of(
            USER,
            MANAGER,
            ADMIN,
            GATEWAY
    );

    private UserRole() {
    }

    public static String canonicalize(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        String canonicalRole = role.trim().toUpperCase(Locale.ROOT);
        if (canonicalRole.startsWith("ROLE_")) {
            return canonicalRole.substring(5);
        }
        return canonicalRole;
    }

    public static String normalizeAccountRoleOrDefault(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }

        String canonicalRole = canonicalize(role);
        if (ACCOUNT_ROLES.contains(canonicalRole)) {
            return canonicalRole;
        }
        throw new IllegalArgumentException("Invalid account role: " + role);
    }

    /**
     * @deprecated 계정 role 기본값 보정에는 {@link #normalizeAccountRoleOrDefault(String)}를,
     * 권한 판정에는 strict predicate를 사용한다.
     */
    @Deprecated
    public static String normalize(String role) {
        return normalizeAccountRoleOrDefault(role);
    }

    public static boolean isValidAccountRole(String role) {
        return ACCOUNT_ROLES.contains(canonicalize(role));
    }

    public static boolean isKnownPrincipalRole(String role) {
        return PRINCIPAL_ROLES.contains(canonicalize(role));
    }

    /**
     * @deprecated 계정 role 검증에는 {@link #isValidAccountRole(String)}를 사용한다.
     */
    @Deprecated
    public static boolean isValidRole(String role) {
        return isValidAccountRole(role);
    }

    public static boolean hasAnyRole(String role, String... allowedRoles) {
        String canonicalRole = canonicalize(role);
        if (canonicalRole == null || allowedRoles == null || allowedRoles.length == 0) {
            return false;
        }

        for (String allowedRole : allowedRoles) {
            if (canonicalRole.equals(canonicalize(allowedRole))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(String role) {
        return ADMIN.equals(canonicalize(role));
    }

    public static boolean isManager(String role) {
        return MANAGER.equals(canonicalize(role));
    }

    public static boolean isUser(String role) {
        return USER.equals(canonicalize(role));
    }

    public static boolean isGateway(String role) {
        return GATEWAY.equals(canonicalize(role));
    }
}
