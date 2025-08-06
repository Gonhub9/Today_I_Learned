package gon.til.domain.dto.card;

import gon.til.domain.dto.tag.TagResponse;
import gon.til.domain.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CardResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final Integer position;
    private final List<TagResponse> tags;

    public static CardResponse from(Card card) {
        List<TagResponse> tagResponses = card.getCardTags().stream()
                .map(cardTag -> TagResponse.from(cardTag.getTag()))
                .collect(Collectors.toList());

        return new CardResponse(
                card.getId(),
                card.getTitle(),
                card.getContent(),
                card.getPosition(),
                tagResponses
        );
    }
}
