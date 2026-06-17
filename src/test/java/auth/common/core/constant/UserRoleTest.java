package auth.common.core.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRoleTest {

    @Test
    void isUser_gatewayRole_returnsFalse() {
        assertThat(UserRole.isUser("GATEWAY")).isFalse();
    }

    @Test
    void isUser_roleGatewayAuthority_returnsFalse() {
        assertThat(UserRole.isUser("ROLE_GATEWAY")).isFalse();
    }

    @Test
    void isGateway_roleGatewayAuthority_returnsTrue() {
        assertThat(UserRole.isGateway("ROLE_GATEWAY")).isTrue();
    }

    @Test
    void isUser_unknownRole_returnsFalse() {
        assertThat(UserRole.isUser("unknown")).isFalse();
    }

    @Test
    void normalizeAccountRoleOrDefault_nullRole_returnsUser() {
        assertThat(UserRole.normalizeAccountRoleOrDefault(null)).isEqualTo(UserRole.USER);
    }

    @Test
    void normalizeAccountRoleOrDefault_gatewayRole_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> UserRole.normalizeAccountRoleOrDefault("GATEWAY"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
