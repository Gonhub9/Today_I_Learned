package gon.til.domain.service;

import gon.til.domain.dto.card.CardCreateRequest;
import gon.til.domain.dto.card.CardShiftRequest;
import gon.til.domain.dto.card.CardUpdateRequest;
import gon.til.domain.entity.Card;
import gon.til.domain.entity.KanbanColumn;
import gon.til.domain.entity.Tag;
import gon.til.domain.entity.User;
import gon.til.domain.repository.CardRepository;
import gon.til.domain.repository.CardTagRepository;
import gon.til.domain.repository.KanbanColumnRepository;
import gon.til.domain.repository.TagRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final KanbanColumnRepository kanbanColumnRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CardTagRepository cardTagRepository;

    // TODO: 카드 생성
    @Transactional
    public Card createCard(Long columnId, Long userId, CardCreateRequest request) {
        KanbanColumn column = getColumnById(columnId);
        User user = getUserById(userId);

        validateColumnOwnership(column, userId);

        Card card = Card.builder()
                .kanbanColumn(column)
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return cardRepository.save(card);
    }

    // TODO: 카드 상세 정보 조회
    public Card getCard(Long cardId, Long userId) {
        Card card = getCardById(cardId);

        validateCardOwnership(card, userId);
        return card;
    }

    // TODO: 카드 수정 (내용, 마감일 등)
    @Transactional
    public Card updateCard(Long cardId, Long userId, CardUpdateRequest request) {
        Card card = getCardById(cardId);

        validateCardOwnership(card, userId);

        card.updateCard(request.getTitle(), request.getContent());

        return card;
    }

    // TODO: 카드 이동 (다른 컬럼으로)
    @Transactional
    public void shiftCard(Long cardId, Long userId, CardShiftRequest request) {
        // 1. 이동 대상 카드와 원래 위치 정보 가져오기
        Card cardToMove = getCardById(cardId);
        validateCardOwnership(cardToMove, userId);

        Long oldColumnId = cardToMove.getKanbanColumn().getId();
        Integer oldPosition = cardToMove.getPosition();

        Long newColumnId = request.getNewColumnId();
        Integer newPosition = request.getNewPosition();

        if (oldColumnId.equals(newColumnId) && oldPosition.equals(newPosition)) {
            return;
        }

        // 2. 카드가 원래 있던 컬럼에서 position 재정렬
        cardRepository.decrementPositionsAfter(oldColumnId, oldPosition);

        cardRepository.incrementPositionsFrom(newColumnId, newPosition);

        KanbanColumn newKanbanColumn = getColumnById(newColumnId);

        cardToMove.updatePosition(newKanbanColumn, newPosition);
    }

    // TODO: 카드 삭제
    @Transactional
    public void deleteCard(Long cardId, Long userId) {
        Card card = getCardById(cardId);
        validateCardOwnership(card, userId);

        cardRepository.delete(card);
    }

    // TODO: 카드에 태그 추가
    @Transactional
    public Card addTagToCard(Long cardId, Long tagId, Long userId) {
        Card card = getCardById(cardId);
        validateCardOwnership(card, userId);

        Tag tag = getTagById(tagId);
        validateTagOwnership(tag, userId);

        // 태그가 카드의 프로젝트와 동일한 프로젝트에 속하는지 확인
        if (!card.getKanbanColumn().getBoard().getProject().getId().equals(tag.getProject().getId())) {
            throw new GlobalException(GlobalErrorCode.TAG_NOT_IN_SAME_PROJECT);
        }

        card.addTag(tag);

        return card;
    }

    // TODO: 카드에 태그 삭제
    @Transactional
    public void removeTagFromCard(Long cardId, Long tagId, Long userId) {
        Card card = getCardById(cardId);
        validateCardOwnership(card, userId);

        Tag tag = getTagById(tagId);
        validateTagOwnership(tag, userId);

        card.removeTag(tagId);
    }

    // 헬퍼 메소드
    private KanbanColumn getColumnById(Long columnId) {
        return kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_COLUMN));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_USER));
    }

    private Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_CARD));
    }

    private Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_TAG));
    }

    /**
     * 사용자가 특정 컬럼에 대한 소유권(수정/삭제 권한)이 있는지 확인합니다.
     * 컬럼 -> 보드 -> 프로젝트 -> 사용자 순으로 소유 관계를 확인합니다.
     */
    private void validateColumnOwnership(KanbanColumn column, Long userId) {
        Long boardOwnerId = column.getBoard().getProject().getUser().getId();
        if (!boardOwnerId.equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_COLUMN);
        }
    }

    /**
     * 사용자가 특정 카드에 대한 소유권(수정/삭제 권한)이 있는지 확인합니다.
     * 카드 -> 컬럼 -> 보드 -> 프로젝트 -> 사용자 순으로 소유 관계를 확인합니다.
     */
    private void validateCardOwnership(Card card, Long userId) {
        Long cardOwnerId = card.getKanbanColumn().getBoard().getProject().getUser().getId();
        if (!cardOwnerId.equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_CARD);
        }
    }

    /**
     * 사용자가 특정 태그에 대한 소유권(수정/삭제 권한)이 있는지 확인합니다.
     * 태그 -> 프로젝트 -> 사용자 순으로 소유 관계를 확인합니다.
     */
    private void validateTagOwnership(Tag tag, Long userId) {
        Long tagOwnerId = tag.getProject().getUser().getId();
        if (!tagOwnerId.equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_TAG);
        }
    }
}
