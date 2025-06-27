package gon.til.RepositoryTest;

import static org.assertj.core.api.Assertions.assertThat;

import gon.til.TilApplication;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(classes = TilApplication.class)
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("프로젝트 저장 및 조회 테스트")
    void Test1() {
        // Given
        User user = new User("testuser", "test1@test.com", "password123");
        User savedUser1 = userRepository.save(user);

        Project project = new Project("Spring Boot 학습", "스프링부트 TIL 프로젝트", "Backend", savedUser1);

        // When
        Project savedProject = projectRepository.save(project);

        // Then
        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getTitle()).isEqualTo("Spring Boot 학습");
        assertThat(savedProject.getUser()).isEqualTo(savedUser1);
    }

    @Test
    @DisplayName("사용자별 프로젝트 조회 테스트")
    void Test2() {
        // Given
        User user = new User("testuser", "test2@test.com", "123");
        User savedUser2 = userRepository.save(user);

        Project project1 = new Project("Spring Boot 학습", "스프링부트 프로젝트", "Backend", savedUser2);
        Project project2 = new Project("알고리즘 풀이", "코딩테스트 대비", "Algorithm", savedUser2);

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.flush();

        // When
        List<Project> projects = projectRepository.findByUser(savedUser2);

        // Then
        assertThat(projects).hasSize(2);
        assertThat(projects.getFirst().getUser()).isEqualTo(savedUser2);
    }
}
