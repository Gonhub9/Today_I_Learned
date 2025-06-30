package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gon.til.TilApplication;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.ProjectService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TilApplication.class)
@Transactional
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 각 테스트마다 사용할 테스트 사용자 생성
        testUser = new User("testuser", "test@test.com", "password123");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("프로젝트 생성 성공 테스트")
    void createProject_Success() {
        // Given
        String title = "Spring Boot 학습";
        String description = "스프링 부트 TIL 프로젝트";
        String category = "Backend";

        // When
        Project createdProject = projectService.createProject(
                testUser.getId(), title, description, category);

        // Then
        assertThat(createdProject.getId()).isNotNull();
        assertThat(createdProject.getTitle()).isEqualTo(title);
        assertThat(createdProject.getDescription()).isEqualTo(description);
        assertThat(createdProject.getCategory()).isEqualTo(category);
        assertThat(createdProject.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("중복 프로젝트 제목으로 생성 실패 테스트")
    void createProject_DuplicateTitle_ThrowsException() {
        // Given
        String duplicateTitle = "중복 프로젝트";
        String description = "설명";
        String category = "Backend";

        // 첫 번째 프로젝트 생성
        projectService.createProject(testUser.getId(), duplicateTitle, description, category);

        // When & Then
        assertThatThrownBy(() ->
                projectService.createProject(testUser.getId(), duplicateTitle, description, category)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_PROJECT_TITLE);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 프로젝트 생성 시 FK 제약조건 에러")
    void createProject_UserNotFound_ThrowsException() {
        // Given
        Long nonExistentUserId = 999L;
        String title = "테스트 프로젝트";
        String description = "설명";
        String category = "Backend";

        // When & Then
        // getReferenceById를 사용하므로 FK 제약조건 에러가 발생
        assertThatThrownBy(() ->
                projectService.createProject(nonExistentUserId, title, description, category)
        )
                .isInstanceOf(Exception.class);  // FK 제약조건 위반 에러
    }

    @Test
    @DisplayName("사용자별 프로젝트 목록 조회 성공 테스트")
    void getUserProjects_Success() {
        // Given
        Project project1 = projectService.createProject(
                testUser.getId(), "프로젝트 1", "설명 1", "Backend");
        Project project2 = projectService.createProject(
                testUser.getId(), "프로젝트 2", "설명 2", "Frontend");

        // When
        List<Project> projects = projectService.getUserProjects(testUser.getId());

        // Then
        assertThat(projects).hasSize(2);
        assertThat(projects).extracting("title")
                .containsExactlyInAnyOrder("프로젝트 1", "프로젝트 2");
        assertThat(projects).allMatch(p -> p.getUser().getId().equals(testUser.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 프로젝트 조회 실패 테스트")
    void getUserProjects_UserNotFound_ThrowsException() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThatThrownBy(() ->
                projectService.getUserProjects(nonExistentUserId)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("빈 프로젝트 목록 조회 테스트")
    void getUserProjects_EmptyList() {
        // Given
        // testUser는 아직 프로젝트가 없음

        // When
        List<Project> projects = projectService.getUserProjects(testUser.getId());

        // Then
        assertThat(projects).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 삭제 성공 테스트")
    void deleteProject_Success() {
        // Given
        Project project = projectService.createProject(
                testUser.getId(), "삭제할 프로젝트", "설명", "Backend");

        // When
        projectService.deleteProject(project.getId(), testUser.getId());

        // Then
        assertThat(projectRepository.findById(project.getId())).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자의 프로젝트 삭제 실패 테스트")
    void deleteProject_AccessDenied_ThrowsException() {
        // Given
        User tempUser = new User("another", "another@test.com", "123");
        User anotherUser = userRepository.save(tempUser);

        Project project = projectService.createProject(
                testUser.getId(), "내 프로젝트", "설명", "Backend");

        // When & Then
        assertThatThrownBy(() ->
                projectService.deleteProject(project.getId(), anotherUser.getId())
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.ACCESS_DENIED_PROJECT);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 삭제 실패 테스트")
    void deleteProject_ProjectNotFound_ThrowsException() {
        // Given
        Long nonExistentProjectId = 999L;

        // When & Then
        assertThatThrownBy(() ->
                projectService.deleteProject(nonExistentProjectId, testUser.getId())
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_PROJECT);
    }

    @Test
    @DisplayName("다른 사용자들의 프로젝트 분리 테스트")
    void getUserProjects_IsolatedByUser() {
        // Given
        User user1 = testUser;
        User user2 = new User("user2", "user2@test.com", "password");
        user2 = userRepository.save(user2);

        // 각 사용자별로 프로젝트 생성
        Project project1 = projectService.createProject(user1.getId(), "User1 프로젝트", "설명", "Backend");
        Project project2 = projectService.createProject(user2.getId(), "User2 프로젝트", "설명", "Frontend");

        // When
        List<Project> user1Projects = projectService.getUserProjects(user1.getId());
        List<Project> user2Projects = projectService.getUserProjects(user2.getId());

        // Then
        assertThat(user1Projects).hasSize(1);
        assertThat(user1Projects.getFirst().getTitle()).isEqualTo("User1 프로젝트");

        assertThat(user2Projects).hasSize(1);
        assertThat(user2Projects.getFirst().getTitle()).isEqualTo("User2 프로젝트");
    }
}