package gon.til.domain.dto.board;

import gon.til.domain.dto.kanbancolumn.KanbanColumnResponse;
import gon.til.domain.dto.project.ProjectResponse;
import gon.til.domain.entity.Board;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class BoardResponse {
    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final ProjectResponse project;
    private final List<KanbanColumnResponse> columns;

    public BoardResponse(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        this.project = new ProjectResponse(board.getProject());
        this.columns = board.getColumns().stream()
                .map(KanbanColumnResponse::new)
                .collect(Collectors.toList());
    }
}
