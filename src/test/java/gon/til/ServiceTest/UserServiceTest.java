package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gon.til.TilApplication;
import gon.til.domain.dto.user.UserSignupRequest;
import gon.til.domain.entity.User;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.UserService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TilApplication.class)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("사용자 생성 성공 테스트")
    void createUser_Success() {
        // Given
        UserSignupRequest request = new UserSignupRequest("testuser", "test@test.com", "password123");

        // When
        User createdUser = userService.createUser(request);

        // Then
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo(request.getUsername());
        assertThat(createdUser.getEmail()).isEqualTo(request.getEmail());
        // 비밀번호가 암호화되어 저장되었는지 확인
        assertThat(createdUser.getPassword()).isNotEqualTo(request.getPassword());
        assertThat(passwordEncoder.matches(request.getPassword(), createdUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("중복 이메일로 사용자 생성 실패 테스트")
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        UserSignupRequest request1 = new UserSignupRequest("user1", "duplicate@test.com", "password123");
        userService.createUser(request1);

        UserSignupRequest request2 = new UserSignupRequest("user2", "duplicate@test.com", "password123");

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request2))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_USER_EMAIL);
    }

    @Test
    @DisplayName("중복 사용자명으로 사용자 생성 실패 테스트")
    void createUser_DuplicateUsername_ThrowsException() {
        // Given
        UserSignupRequest request1 = new UserSignupRequest("duplicate", "email1@test.com", "password123");
        userService.createUser(request1);

        UserSignupRequest request2 = new UserSignupRequest("duplicate", "email2@test.com", "password123");

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request2))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_USER_NAME);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공 테스트")
    void getUserByEmail_Success() {
        // Given
        UserSignupRequest request = new UserSignupRequest("testuser", "test@test.com", "password123");
        User createdUser = userService.createUser(request);

        // When
        User foundUser = userService.getUserByEmail(request.getEmail());

        // Then
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getUsername()).isEqualTo(request.getUsername());
        assertThat(foundUser.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자 조회 실패 테스트")
    void getUserByEmail_NotFound_ThrowsException() {
        // Given
        String nonExistentEmail = "notfound@test.com";

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(nonExistentEmail))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_USER_EMAIL);
    }
}
