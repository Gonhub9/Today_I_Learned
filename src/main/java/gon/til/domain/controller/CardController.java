package gon.til.domain.controller;

import gon.til.domain.dto.card.CardCreateRequest;
import gon.til.domain.dto.card.CardResponse;
import gon.til.domain.dto.card.CardShiftRequest;
import gon.til.domain.dto.card.CardUpdateRequest;
import gon.til.domain.entity.User;
import gon.til.domain.service.CardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Card", description = "카드 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    // 카드 전체 조회
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CardResponse>> getAllCards(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal User user
    ) {
        List<CardResponse> cards = cardService.findAllCards(projectId, user.getId());

        return ResponseEntity.ok(cards);
    }

    // 카드 상세 조회
    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCardId(
            @PathVariable("cardId") Long cardId,
            @AuthenticationPrincipal User user
    ) {
        CardResponse card = cardService.getCard(cardId, user.getId());

        return ResponseEntity.ok(card);
    }

    // 카드 생성
    @PostMapping("/columns/{columnId}")
    public ResponseEntity<CardResponse> createCard(
            @PathVariable("columnId") Long columnId,
            @Valid @RequestBody CardCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        CardResponse card = cardService.createCard(columnId, user.getId(), request);

                        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/cards/{id}")
                .buildAndExpand(card.getId())
                .toUri();

        return ResponseEntity.created(location).body(card);
    }

    // 카드 삭제
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable("cardId") Long cardId,
            @AuthenticationPrincipal User user
    ) {
        cardService.deleteCard(cardId, user.getId());

        return ResponseEntity.noContent().build();
    }

    // 카드 수정
    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable("cardId") Long cardId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CardUpdateRequest request
    ) {
        CardResponse card = cardService.updateCard(cardId, user.getId(), request);

        return ResponseEntity.ok(card);
    }

    @PatchMapping("/{cardId}/shift")
    public ResponseEntity<CardResponse> shiftCard(
            @PathVariable("cardId") Long cardId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CardShiftRequest request
    ) {
        CardResponse card = cardService.shiftCard(cardId, user.getId(), request);

        return ResponseEntity.ok(card);
    }

}