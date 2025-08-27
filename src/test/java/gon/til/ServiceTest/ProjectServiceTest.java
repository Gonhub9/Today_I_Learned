package gon.til.ServiceTest;

import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.project.ProjectResponse;
import gon.til.domain.dto.project.ProjectUpdateRequest;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.ProjectService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 단위 테스트")
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Nested
    @DisplayName("프로젝트 생성")
    class CreateProject {
        @Test
        @DisplayName("성공")
        void createProject_Success() {
            // Given
            Long userId = 1L;
            User testUser = User.builder().id(userId).build();
            ProjectCreateRequest request = new ProjectCreateRequest("새 프로젝트", "설명", "BE");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.getReferenceById(userId)).thenReturn(testUser);
            when(projectRepository.existsByTitleAndUser(request.getTitle(), testUser)).thenReturn(false);
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
                Project project = invocation.getArgument(0);
                return Project.builder()
                        .id(1L)
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .category(project.getCategory())
                        .user(project.getUser())
                        .build();
            });

            // When
            ProjectResponse createdProject = projectService.createProject(userId, request);

            // Then
            assertThat(createdProject.getId()).isNotNull();
            assertThat(createdProject.getTitle()).isEqualTo(request.getTitle());
        }

        @Test
        @DisplayName("실패 - 중복된 제목")
        void createProject_DuplicateTitle_ThrowsException() {
            // Given
            Long userId = 1L;
            User testUser = User.builder().id(userId).build();
            ProjectCreateRequest request = new ProjectCreateRequest("중복 프로젝트", "설명", "BE");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(projectRepository.existsByTitleAndUser(request.getTitle(), testUser)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> projectService.createProject(userId, request))
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
            Long userId = 1L;
            Long projectId = 1L;
            User testUser = User.builder().id(userId).build();
            Project project = Project.builder().id(projectId).user(testUser).title("원본").description("원본 설명").category("FE").build();
            ProjectUpdateRequest updateRequest = new ProjectUpdateRequest("수정", "수정 설명", "BE");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

            // When
            ProjectResponse updatedProject = projectService.updateProject(projectId, userId, updateRequest);

            // Then
            assertThat(updatedProject.getTitle()).isEqualTo(updateRequest.getTitle());
            assertThat(updatedProject.getDescription()).isEqualTo(updateRequest.getDescription());
            assertThat(updatedProject.getCategory()).isEqualTo(updateRequest.getCategory());
        }

        @Test
        @DisplayName("실패 - 다른 사용자")
        void updateProject_AccessDenied_ThrowsException() {
            // Given
            Long ownerId = 1L;
            Long attackerId = 2L;
            Long projectId = 1L;
            User owner = User.builder().id(ownerId).build();
            Project project = Project.builder().id(projectId).user(owner).build();
            ProjectUpdateRequest updateRequest = new ProjectUpdateRequest("수정 시도", "", "");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

            // When & Then
            assertThatThrownBy(() -> projectService.updateProject(projectId, attackerId, updateRequest))
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.ACCESS_DENIED_PROJECT);
        }
    }
}
