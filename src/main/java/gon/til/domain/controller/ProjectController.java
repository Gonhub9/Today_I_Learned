package gon.til.domain.controller;

import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.project.ProjectResponse;
import gon.til.domain.dto.project.ProjectUpdateRequest;
import gon.til.domain.entity.User;
import gon.til.domain.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Project", description = "프로젝트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @AuthenticationPrincipal User user
    ) {
        List<ProjectResponse> projects = projectService.getUserProjects(user.getId());

        return ResponseEntity.ok(projects);
    }

    // 상세 조회
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @AuthenticationPrincipal User user,
            @PathVariable("projectId") Long projectId
    ) {
        ProjectResponse projectResponse = projectService.getProjectById(user.getId(), projectId);

        return ResponseEntity.ok(projectResponse);
    }

    // 프로젝트 생성
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        ProjectResponse projectResponse = projectService.createProject(user.getId(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/projects/{id}")
                .buildAndExpand(projectResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(projectResponse);
    }

    // 프로젝트 수정
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @AuthenticationPrincipal User user,
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        ProjectResponse projectResponse = projectService.updateProject(projectId, user.getId(), request);

        return ResponseEntity.ok(projectResponse);
    }

    // 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @AuthenticationPrincipal User user,
            @PathVariable("projectId") Long projectId
    ) {
        projectService.deleteProject(projectId, user.getId());

        return ResponseEntity.noContent().build();
    }
}