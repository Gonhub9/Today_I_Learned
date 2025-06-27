package gon.til.domain.service;

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
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 프로젝트 생성
    public Project createProject(Long userId, String title, String description, String category) {

        // 1. 프로젝트 이름 검증
        validateProject(userId, title);

        // 2. 불필요한 User 조회 방지
        User user = userRepository.getReferenceById(userId);

        // 3. 프로젝트 생성
        Project project = new Project(title, description, category, user);
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

//    public Project updateProject() {
//
//    }

    // 프로젝트 검증
    private void validateProject(Long userId, String title) {

        // Project 이름 중복 검사
        if (projectRepository.existsByUserAndTitle(userId, title)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_PROJECT_TITLE);
        }
    }
}
