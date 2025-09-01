package gon.til.domain.service;

import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.project.ProjectResponse;
import gon.til.domain.dto.project.ProjectUpdateRequest;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 프로젝트 생성
    @Transactional
    public ProjectResponse createProject(Long userId, ProjectCreateRequest request) {

        // 1. 프로젝트 이름 검증
        validateProject(userId, request.getTitle());

        // 2. 불필요한 User 조회 방지
        User user = userRepository.getReferenceById(userId);

        // 3. 프로젝트와 기본 보드, 컬럼 생성
        Project project = Project.createWithDefaultBoard(
            request.getTitle(),
            request.getDescription(),
            request.getCategory(),
            user
        );

        Project savedProject = projectRepository.save(project);

        return ProjectResponse.from(savedProject);
    }

    // 프로젝트 리스트
    public List<ProjectResponse> getUserProjects(Long userId) {

        // 1. 유저 존재 여부 확인 (Optional)
        if (!userRepository.existsById(userId)) {
            throw new GlobalException(GlobalErrorCode.NOT_FOUND_USER);
        }

        // 2. 최적화된 메서드를 사용하여 Project 리스트 조회
        List<Project> projects = projectRepository.findByUserId(userId);

        // 3. Project 리스트 반환
        return projects.stream()
            .map(ProjectResponse::from)
            .collect(Collectors.toList());
    }

    // 프로젝트 상세 조회
    public ProjectResponse getProjectById(Long userId, Long projectId) {
        Project project = validateProjectOwnership(projectId, userId);
        return ProjectResponse.from(project);
    }

    // 프로젝트 삭제
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = validateProjectOwnership(projectId, userId);
        projectRepository.delete(project);
    }

    // 프로젝트 수정
    @Transactional
    public ProjectResponse updateProject(Long projectId, Long userId, ProjectUpdateRequest request) {

        Project project = validateProjectOwnership(projectId, userId);

        // 업데이트 전 중복 검사
        if (projectRepository.existsByTitleAndUserIdAndIdNot(request.getTitle(), userId, projectId)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_PROJECT_TITLE);
        }

        project.updateProject(request.getTitle(), request.getDescription(), request.getCategory());

        return ProjectResponse.from(project);
    }

    // 프로젝트 검증
    private void validateProject(Long userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_USER));

        // Project 이름 중복 검사
        if (projectRepository.existsByTitleAndUser(title, user)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_PROJECT_TITLE);
        }
    }

    private Project validateProjectOwnership(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_PROJECT));

        if (!project.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_PROJECT);
        }
        return project;
    }
}