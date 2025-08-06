package gon.til.domain.dto.kanbancolumn;

import gon.til.domain.entity.KanbanColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KanbanColumnResponse {

    private final Long id;
    private final String title;
    private final Integer position;

    public static KanbanColumnResponse from(KanbanColumn kanbanColumn) {
        return new KanbanColumnResponse(
            kanbanColumn.getId(),
            kanbanColumn.getTitle(),
            kanbanColumn.getPosition()
        );
    }
}
