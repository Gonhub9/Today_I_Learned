package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gon.til.TilApplication;
import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.project.ProjectUpdateRequest;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.ProjectService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TilApplication.class)
@Transactional
@DisplayName("ProjectService 테스트")
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
        testUser = new User("testuser", "test@test.com", "password123");
        userRepository.save(testUser);
    }

    @Nested
    @DisplayName("프로젝트 생성")
    class CreateProject {
        @Test
        @DisplayName("성공")
        void createProject_Success() {
            // Given
            ProjectCreateRequest request = new ProjectCreateRequest("새 프로젝트", "설명", "BE");

            // When
            Project createdProject = projectService.createProject(testUser.getId(), request);

            // Then
            assertThat(createdProject.getId()).isNotNull();
            assertThat(createdProject.getTitle()).isEqualTo(request.getTitle());
            assertThat(createdProject.getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("실패 - 중복된 제목")
        void createProject_DuplicateTitle_ThrowsException() {
            // Given
            ProjectCreateRequest request = new ProjectCreateRequest("중복 프로젝트", "설명", "BE");
            projectService.createProject(testUser.getId(), request);

            // When & Then
            assertThatThrownBy(() -> projectService.createProject(testUser.getId(), request))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_PROJECT_TITLE);
        }
    }

    @Nested
    @DisplayName("프로젝트 수정")
    class UpdateProject {
        @Test
        @DisplayName("성공")
        void updateProject_Success() {
            // Given
            ProjectCreateRequest createRequest = new ProjectCreateRequest("원본", "원본 설명", "FE");
            Project project = projectService.createProject(testUser.getId(), createRequest);
            ProjectUpdateRequest updateRequest = new ProjectUpdateRequest("수정", "수정 설명", "BE");

            // When
            Project updatedProject = projectService.updateProject(project.getId(), testUser.getId(), updateRequest);

            // Then
            assertThat(updatedProject.getTitle()).isEqualTo(updateRequest.getTitle());
            assertThat(updatedProject.getDescription()).isEqualTo(updateRequest.getDescription());
            assertThat(updatedProject.getCategory()).isEqualTo(updateRequest.getCategory());
        }

        @Test
        @DisplayName("실패 - 다른 사용자")
        void updateProject_AccessDenied_ThrowsException() {
            // Given
            User anotherUser = userRepository.save(new User("another", "a@a.com", "123"));
            Project project = projectService.createProject(testUser.getId(), new ProjectCreateRequest("내꺼", "", ""));
            ProjectUpdateRequest updateRequest = new ProjectUpdateRequest("수정 시도", "", "");

            // When & Then
            assertThatThrownBy(() -> projectService.updateProject(project.getId(), anotherUser.getId(), updateRequest))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.ACCESS_DENIED_PROJECT);
        }
    }
}
