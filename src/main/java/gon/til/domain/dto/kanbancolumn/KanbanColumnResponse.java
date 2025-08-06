package gon.til.domain.dto.kanbancolumn;

import gon.til.domain.dto.card.CardResponse;
import gon.til.domain.entity.Card;
import gon.til.domain.entity.KanbanColumn;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class KanbanColumnResponse {
    private final Long id;
    private final String title;
    private final Integer position;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<CardResponse> cards;

    public KanbanColumnResponse(KanbanColumn column) {
        this.id = column.getId();
        this.title = column.getTitle();
        this.position = column.getPosition();
        this.createdAt = column.getCreatedAt();
        this.updatedAt = column.getUpdatedAt();
        this.cards = column.getCards().stream()
                .sorted(Comparator.comparing(Card::getPosition))
                .map(CardResponse::new)
                .collect(Collectors.toList());
    }
}
