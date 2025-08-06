package gon.til.domain.dto.card;

import gon.til.domain.dto.tag.TagResponse;
import gon.til.domain.entity.Card;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class CardResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<TagResponse> tags;

    public CardResponse(Card card) {
        this.id = card.getId();
        this.title = card.getTitle();
        this.content = card.getContent();
        this.createdAt = card.getCreatedAt();
        this.updatedAt = card.getUpdatedAt();
        this.tags = card.getCardTags().stream()
                .map(cardTag -> new TagResponse(cardTag.getTag()))
                .collect(Collectors.toList());
    }
}
