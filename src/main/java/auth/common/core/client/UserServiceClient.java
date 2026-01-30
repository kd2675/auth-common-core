package auth.common.core.client;

import auth.common.core.dto.UserCreateRequest;
import auth.common.core.dto.UserDto;
import auth.common.core.dto.UserUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Service Feign Client
 * - 다른 서비스에서 User 정보 조회 시 사용
 * - auth-back-server의 /api/users API 호출
 */
@FeignClient(name = "auth-back-server", path = "/api/users")
public interface UserServiceClient {

    /**
     * ID로 User 조회
     */
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    /**
     * Username으로 User 조회
     */
    @GetMapping("/username/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    /**
     * Email로 User 조회
     */
    @GetMapping("/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);

    /**
     * 모든 User 조회
     */
    @GetMapping
    List<UserDto> getAllUsers();

    /**
     * User 생성
     */
    @PostMapping
    UserDto createUser(@RequestBody UserCreateRequest request);

    /**
     * User 수정
     */
    @PutMapping("/{id}")
    UserDto updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateRequest request);

    /**
     * User 삭제
     */
    @DeleteMapping("/{id}")
    void deleteUser(@PathVariable("id") Long id);

    /**
     * User 존재 여부 확인
     */
    @GetMapping("/{id}/exists")
    boolean existsById(@PathVariable("id") Long id);
}
