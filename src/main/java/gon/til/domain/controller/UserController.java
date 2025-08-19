package gon.til.domain.controller;

import gon.til.domain.dto.user.UserLoginRequest;
import gon.til.domain.dto.user.UserResponse;
import gon.til.domain.dto.user.UserSignupRequest;
import gon.til.domain.entity.User;
import gon.til.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User user) {

        // 파라미터로 받은 user 객체엔 현재 로그인한 사용자의 모든 정보가 있음
        // user.getId(), user.getEmail(), user.getUsername() 등 바로 사용 가능
        // DB 또 조회 안해도 됨

        if (user == null) {
            // 이 경우는 보통 토큰이 없거나 유효하지 않을 때 발생
            // 그 전에 필터 레벨에서 차단되므로 거의 들어올 일은 없음
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // User 엔티티를 UserResponse DTO로 변환 후 반환
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody
            UserLoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().build();
    }
}
