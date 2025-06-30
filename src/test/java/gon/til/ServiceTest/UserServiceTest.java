package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gon.til.TilApplication;
import gon.til.domain.entity.User;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.UserService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TilApplication.class)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 생성 성공 테스트")
    void createUser_Success() {
        // Given
        String username = "testuser";
        String email = "test@test.com";
        String password = "password123";

        // When
        User createdUser = userService.createUser(username, email, password);

        // Then
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo(username);
        assertThat(createdUser.getEmail()).isEqualTo(email);
        assertThat(createdUser.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("중복 이메일로 사용자 생성 실패 테스트")
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        String username1 = "user1";
        String username2 = "user2";
        String duplicateEmail = "duplicate@test.com";
        String password = "password123";

        // 첫 번째 사용자 생성
        userService.createUser(username1, duplicateEmail, password);

        // When & Then
        assertThatThrownBy(() ->
                userService.createUser(username2, duplicateEmail, password)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_USER_EMAIL);
    }

    @Test
    @DisplayName("중복 사용자명으로 사용자 생성 실패 테스트")
    void createUser_DuplicateUsername_ThrowsException() {
        // Given
        String duplicateUsername = "duplicate";
        String email1 = "email1@test.com";
        String email2 = "email2@test.com";
        String password = "password123";

        // 첫 번째 사용자 생성
        userService.createUser(duplicateUsername, email1, password);

        // When & Then
        assertThatThrownBy(() ->
                userService.createUser(duplicateUsername, email2, password)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_USER_NAME);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공 테스트")
    void getUserByEmail_Success() {
        // Given
        String username = "testuser";
        String email = "test@test.com";
        String password = "password123";

        User createdUser = userService.createUser(username, email, password);

        // When
        User foundUser = userService.getUserByEmail(email);

        // Then
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자 조회 실패 테스트")
    void getUserByEmail_NotFound_ThrowsException() {
        // Given
        String nonExistentEmail = "notfound@test.com";

        // When & Then
        assertThatThrownBy(() ->
                userService.getUserByEmail(nonExistentEmail)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_USER_EMAIL);
    }
}