package gon.til.domain.dto.project;

import gon.til.domain.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final Long mainBoardId;

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getTitle(),
            project.getDescription(),
            project.getCategory(),
            project.getBoard() != null ? project.getBoard().getId() : null
        );
    }
}
