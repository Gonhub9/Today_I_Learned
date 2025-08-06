package gon.til.domain.dto.project;

import gon.til.domain.entity.Project;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ProjectResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.category = project.getCategory();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
    }
}
