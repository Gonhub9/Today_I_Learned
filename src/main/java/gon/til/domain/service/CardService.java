package gon.til.domain.service;

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
    public Card createCard(Long columnId, Long userId, String title, String content) {
        KanbanColumn column = getColumnById(columnId);
        User user = getUserById(userId);

        validateColumnOwnership(column, userId);

        Card card = Card.builder()
                .kanbanColumn(column)
                .user(user)
                .title(title)
                .content(content)
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
    public Card updateCard(Long cardId, Long userId, String newTitle, String newContent) {
        Card card = getCardById(cardId);

        validateCardOwnership(card, userId);

        card.updateCard(newTitle, newContent);

        return card;
    }

    // TODO: 카드 이동 (다른 컬럼으로)
    public void shiftCard(Long cardId, Long newColumnId, Integer newPosition) {
        // 1. 이동 대상 카드와 원래 위치 정보 가져오기
        Card cardToMove = getCardById(cardId);
        Long oldColumnId = cardToMove.getKanbanColumn().getId();
        Integer oldPosition = cardToMove.getPosition();

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
    public void deleteCard(Long cardId) {
        Card card = getCardById(cardId);

        cardRepository.delete(card);
    }

    // TODO: 카드에 태그 추가
    public Card addTagToCard(Long cardId, Long tagId) {
        Card card = getCardById(cardId);

        Tag tag = getTagById(tagId);

        card.addTag(tag);

        return card;
    }

    // TODO: 카드에 태그 삭제
    @Transactional
    public void removeTagFromCard(Long cardId, Long tagId) {
        Card card = getCardById(cardId);

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
}
