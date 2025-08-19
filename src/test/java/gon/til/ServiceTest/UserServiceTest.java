package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gon.til.domain.dto.user.UserSignupRequest;
import gon.til.domain.entity.User;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.UserService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        // when(userRepository.existsByNickname(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User createdUser = userService.createUser(request);

        // Then
        // DB에 저장된 필드 값 검증
        assertThat(createdUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(createdUser.getPassword()).isEqualTo("encodedPassword");

        // 비즈니스 로직 및 Spring Security 연동 검증
        // getUsername()은 로그인 ID로 사용될 이메일을 반환하는지 확인
        assertThat(createdUser.getUsername()).isEqualTo(request.getEmail());
        // getDisplayName()은 실제 사용자 이름(닉네임)을 반환하는지 확인
        assertThat(createdUser.getDisplayName()).isEqualTo(request.getUsername());
    }

    @Test
    @DisplayName("중복 이메일로 사용자 생성 실패 테스트")
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        UserSignupRequest request = new UserSignupRequest("user2", "duplicate@test.com", "password123");
        // when(userRepository.existsByNickname(request.getUsername())).thenReturn(false);
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
        when(userRepository.existsByDisplayName(request.getUsername())).thenReturn(true);

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
