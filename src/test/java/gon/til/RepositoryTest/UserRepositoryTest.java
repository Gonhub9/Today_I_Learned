package gon.til.RepositoryTest;

import static org.assertj.core.api.Assertions.assertThat;

import gon.til.TilApplication;
import gon.til.entity.User;
import gon.til.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TilApplication.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Order(1)
    @DisplayName("사용자 저장 및 조회 테스트")
    void Test1() {
        // Given
        User user = new User("testuser", "test@test.com", "1234");

        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("test@test.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("이메일로 사용자 조회 테스트")
    void Test2() {

        // When
        Optional<User> found = userRepository.findByEmail("test@test.com");
        Optional<User> notFound = userRepository.findByEmail("notexist@test.com");

        // Then
        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("이메일 중복 체크 테스트")
    void Test3() {
        // When & Then
        assertThat(userRepository.existsByEmail("test@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexist@test.com")).isFalse();
    }
}
