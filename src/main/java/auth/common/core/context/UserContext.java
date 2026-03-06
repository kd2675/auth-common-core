package auth.common.core.context;

import auth.common.core.constant.UserRole;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 인증 컨텍스트
 *
 * Gateway Offloading 패턴:
 * - Cloud-Back-Server(Gateway)가 JWT를 검증하고 다음 헤더를 추가:
 *   - X-User-Key: opaque 사용자 식별자(user.user_key)
 *   - X-User-Name: 사용자 이름
 *   - X-User-Role: 사용자 역할
 * - 모든 마이크로서비스는 이 헤더를 신뢰하고 UserContext로 사용
 *
 * 사용처:
 * - auth-back-server: 권한 체크
 * - zeroq-back-service: 사용자 식별
 * - 다른 모든 마이크로서비스: 인증 정보 사용
 */
@Getter
@Builder
public class UserContext {

    private final String userKey;
    private final String userName;
    private final String role;

    /**
     * 인증된 사용자인지 확인
     */
    public boolean isAuthenticated() {
        return userKey != null;
    }

    /**
     * 매장 관리자 권한인지 확인
     */
    public boolean isManager() {
        return UserRole.isManager(role);
    }

    /**
     * 운영자 권한(ADMIN)인지 확인
     */
    public boolean isAdmin() {
        return UserRole.isAdmin(role);
    }

    /**
     * 일반 사용자 권한인지 확인
     */
    public boolean isUser() {
        return UserRole.isUser(role);
    }

    public boolean isOwner(String targetUserKey) {
        return userKey != null && userKey.equals(targetUserKey);
    }

    public boolean canAccess(String targetUserKey) {
        return isAdmin() || isOwner(targetUserKey);
    }
}
