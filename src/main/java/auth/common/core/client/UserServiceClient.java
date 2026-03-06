package auth.common.core.client;

import auth.common.core.dto.UserCreateRequest;
import auth.common.core.dto.UserDto;
import auth.common.core.dto.UserUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import web.common.core.response.base.dto.ResponseDataDTO;

import java.util.List;

/**
 * User Service Feign Client
 * - 다른 서비스에서 User 정보 조회 시 사용
 * - auth-back-server의 /api/users API 호출
 */
@FeignClient(name = "auth-back-server", path = "/api/users")
public interface UserServiceClient {

    /**
     * user_key(opaque)로 User 조회
     */
    @GetMapping("/{userKey}")
    ResponseDataDTO<UserDto> getUserByUserKey(@PathVariable("userKey") String userKey);

    /**
     * Username으로 User 조회
     */
    @GetMapping("/username/{username}")
    ResponseDataDTO<UserDto> getUserByUsername(@PathVariable("username") String username);

    /**
     * Email로 User 조회
     */
    @GetMapping("/email/{email}")
    ResponseDataDTO<UserDto> getUserByEmail(@PathVariable("email") String email);

    /**
     * 모든 User 조회
     */
    @GetMapping
    ResponseDataDTO<List<UserDto>> getAllUsers();

    /**
     * User 생성
     */
    @PostMapping
    ResponseDataDTO<UserDto> createUser(@RequestBody UserCreateRequest request);

    /**
     * User 수정
     */
    @PutMapping("/{userKey}")
    ResponseDataDTO<UserDto> updateUser(@PathVariable("userKey") String userKey, @RequestBody UserUpdateRequest request);

    /**
     * User 삭제
     */
    @DeleteMapping("/{userKey}")
    ResponseDataDTO<Void> deleteUser(@PathVariable("userKey") String userKey);

    /**
     * User 존재 여부 확인
     */
    @GetMapping("/{userKey}/exists")
    ResponseDataDTO<Boolean> existsByUserKey(@PathVariable("userKey") String userKey);
}
