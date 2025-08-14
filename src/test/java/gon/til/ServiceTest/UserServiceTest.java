package gon.til.ServiceTest;

import gon.til.domain.dto.user.UserSignupRequest;
import gon.til.domain.entity.User;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.UserService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("사용자 생성 성공 테스트")
    void createUser_Success() {
        // Given
        UserSignupRequest request = new UserSignupRequest("testuser", "test@test.com", "password123");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User createdUser = userService.createUser(request);

        // Then
        assertThat(createdUser.getUsername()).isEqualTo(request.getUsername());
        assertThat(createdUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(createdUser.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("중복 이메일로 사용자 생성 실패 테스트")
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        UserSignupRequest request = new UserSignupRequest("user2", "duplicate@test.com", "password123");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_USER_EMAIL);
    }

    @Test
    @DisplayName("중복 사용자명으로 사용자 생성 실패 테스트")
    void createUser_DuplicateUsername_ThrowsException() {
        // Given
        UserSignupRequest request = new UserSignupRequest("duplicate", "email2@test.com", "password123");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_USER_NAME);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공 테스트")
    void getUserByEmail_Success() {
        // Given
        String email = "test@test.com";
        User user = User.builder().email(email).build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        User foundUser = userService.getUserByEmail(email);

        // Then
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자 조회 실패 테스트")
    void getUserByEmail_NotFound_ThrowsException() {
        // Given
        String nonExistentEmail = "notfound@test.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(nonExistentEmail))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_USER_EMAIL);
    }
}
