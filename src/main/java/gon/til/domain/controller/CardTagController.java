package gon.til.domain.controller;

import gon.til.domain.dto.card.CardResponse;
import gon.til.domain.dto.cardtag.CardTagRequest;
import gon.til.domain.entity.User;
import gon.til.domain.service.CardTagService; // 변경
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "CardTag", description = "카드-태그 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardTagController {

    private final CardTagService cardTagService; // 변경

    // 카드에 태그 추가
    @PostMapping("/{cardId}/tags")
    public ResponseEntity<CardResponse> addTagToCard(
            @PathVariable("cardId") Long cardId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CardTagRequest request
    ) {
        CardResponse card = cardTagService.addTagToCard(cardId, request.getTagId(), user.getId()); // 변경

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v1/cards/{id}")
                .buildAndExpand(card.getId())
                .toUri();

        return ResponseEntity.created(location).body(card);
    }

    // 카드에 태그 삭제
    @DeleteMapping("/{cardId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromCard(
            @PathVariable("cardId") Long cardId,
            @AuthenticationPrincipal User user,
            @PathVariable("tagId") Long tagId
    ) {
        cardTagService.removeTagFromCard(cardId, tagId, user.getId()); // 변경 및 버그 수정

        return ResponseEntity.noContent().build();
    }
}
