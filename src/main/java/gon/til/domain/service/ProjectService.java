package gon.til.domain.service;

import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.project.ProjectUpdateRequest;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 프로젝트 생성
    public Project createProject(Long userId, ProjectCreateRequest request) {

        // 1. 프로젝트 이름 검증
        validateProject(userId, request.getTitle());

        // 2. 불필요한 User 조회 방지
        User user = userRepository.getReferenceById(userId);

        // 3. 프로젝트 생성
        Project project = new Project(request.getTitle(), request.getDescription(), request.getCategory(), user);
        return projectRepository.save(project);

    }

    // 프로젝트 리스트
    public List<Project> getUserProjects(Long userId) {

        // 1. 이 유저의 프로젝트가 맞는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_USER));

        // 2. Project 리스트 반환
        return projectRepository.findByUser(user);

    }

    // 프로젝트 삭제
    public void deleteProject(Long projectId,Long userId) {

        // 1. 프로젝트 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_PROJECT));

        // 2. 프로젝트 소유자 체크
        if (!project.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_PROJECT);
        }

        // 3. 프로젝트 삭제
        projectRepository.delete(project);

    }

    // 프로젝트 수정
    @Transactional
    public Project updateProject(Long projectId, Long userId, ProjectUpdateRequest request) {

        Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_PROJECT));

        if (!project.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_PROJECT);
        }

        project.updateProject(request.getTitle(), request.getDescription(), request.getCategory());

        return projectRepository.save(project);

    }

    // 프로젝트 검증
    private void validateProject(Long userId, String title) {

        // Project 이름 중복 검사
        if (projectRepository.existsByUserIdAndTitle(userId, title)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_PROJECT_TITLE);
        }
    }
}
