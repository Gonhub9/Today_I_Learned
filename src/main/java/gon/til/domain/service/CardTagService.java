package gon.til.domain.service;

import gon.til.domain.dto.card.CardResponse;
import gon.til.domain.entity.Card;
import gon.til.domain.entity.Tag;
import gon.til.domain.repository.CardRepository;
import gon.til.domain.repository.TagRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardTagService {

    private final CardRepository cardRepository;
    private final TagRepository tagRepository;

    // 카드에 태그 추가
    @Transactional
    public CardResponse addTagToCard(Long cardId, Long tagId, Long userId) {
        Card card = getCardById(cardId);
        validateCardOwnership(card, userId);

        Tag tag = getTagById(tagId);
        validateTagOwnership(tag, userId);

        // 태그가 카드의 프로젝트와 동일한 프로젝트에 속하는지 확인
        if (!card.getKanbanColumn().getBoard().getProject().getId().equals(tag.getProject().getId())) {
            throw new GlobalException(GlobalErrorCode.TAG_NOT_IN_SAME_PROJECT);
        }

        card.addTag(tag);

        return CardResponse.from(card);
    }

    // 카드에 태그 삭제
    @Transactional
    public void removeTagFromCard(Long cardId, Long tagId, Long userId) {
        Card card = getCardById(cardId);
        validateCardOwnership(card, userId);

        Tag tag = getTagById(tagId);
        validateTagOwnership(tag, userId);

        card.removeTag(tagId);
    }

    // 헬퍼 메소드
    private Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_CARD));
    }

    private Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_TAG));
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
