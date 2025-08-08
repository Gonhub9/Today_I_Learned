package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import gon.til.domain.dto.card.CardCreateRequest;
import gon.til.domain.dto.card.CardShiftRequest;
import gon.til.domain.dto.card.CardUpdateRequest;
import gon.til.domain.entity.Board;
import gon.til.domain.entity.Card;
import gon.til.domain.entity.CardTag;
import gon.til.domain.entity.KanbanColumn;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.Tag;
import gon.til.domain.entity.User;
import gon.til.domain.repository.CardRepository;
import gon.til.domain.repository.KanbanColumnRepository;
import gon.til.domain.repository.TagRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.CardService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService 테스트")
@MockitoSettings(strictness = Strictness.LENIENT)
class CardServiceTest {

    @InjectMocks
    private CardService cardService;

    @Mock
    private CardRepository cardRepository;
    @Mock
    private KanbanColumnRepository kanbanColumnRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;

    private User user;
    private Project project;
    private Board board;
    private KanbanColumn column;
    @Mock
    private Card card;
    private Tag tag;
    private Set<CardTag> cardTagsSet;

    // Mock 객체의 상태를 저장할 변수들
    private String cardTitle;
    private String cardContent;
    private KanbanColumn cardKanbanColumn;
    private Integer cardPosition;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        project = Project.builder().id(1L).user(user).build();
        board = Board.builder().id(1L).project(project).build();
        column = KanbanColumn.builder().id(1L).board(board).position(1).build();
        tag = Tag.builder().id(1L).project(project).name("Test Tag").build();
        cardTagsSet = new HashSet<>();

        // card Mock 객체의 기본 동작 설정
        given(card.getId()).willReturn(1L);
        given(card.getUser()).willReturn(user);

        // card 필드에 대한 doAnswer 설정
        doAnswer(invocation -> {
            return cardTitle;
        }).when(card).getTitle();

        doAnswer(invocation -> {
            return cardContent;
        }).when(card).getContent();

        doAnswer(invocation -> {
            return cardKanbanColumn;
        }).when(card).getKanbanColumn();

        doAnswer(invocation -> {
            return cardPosition;
        }).when(card).getPosition();

        doAnswer(invocation -> {
            return cardTagsSet;
        }).when(card).getCardTags();

        // card.updateCard 메서드에 대한 doAnswer 설정
        doAnswer(invocation -> {
            cardTitle = invocation.getArgument(0);
            cardContent = invocation.getArgument(1);
            return null;
        }).when(card).updateCard(any(String.class), any(String.class));

        // card.updatePosition 메서드에 대한 doAnswer 설정
        doAnswer(invocation -> {
            cardKanbanColumn = invocation.getArgument(0);
            cardPosition = invocation.getArgument(1);
            return null;
        }).when(card).updatePosition(any(KanbanColumn.class), any(Integer.class));

        // card.addTag 메서드에 대한 doAnswer 설정
        doAnswer(invocation -> {
            Tag tagToAdd = invocation.getArgument(0);
            cardTagsSet.add(new CardTag(card, tagToAdd));
            return null;
        }).when(card).addTag(any(Tag.class));

        // card.removeTag 메서드에 대한 doAnswer 설정
        doAnswer(invocation -> {
            Long tagIdToRemove = invocation.getArgument(0);
            cardTagsSet.removeIf(cardTag -> cardTag.getTag()
                    .getId()
                    .equals(tagIdToRemove));
            return null;
        }).when(card).removeTag(any(Long.class));
    }

    @Nested
    @DisplayName("카드 생성")
    class CreateCard {
        @Test
        @DisplayName("성공")
        void createCard_success() {
            // given
            CardCreateRequest request = new CardCreateRequest(column.getId(), "새 카드", "내용");
            given(kanbanColumnRepository.findById(column.getId())).willReturn(Optional.of(column));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(cardRepository.save(any(Card.class))).willAnswer(i -> i.getArgument(0));

            // when
            Card newCard = cardService.createCard(column.getId(), user.getId(), request);

            // then
            assertNotNull(newCard);
            assertEquals(request.getTitle(), newCard.getTitle());
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void createCard_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
            CardCreateRequest request = new CardCreateRequest(column.getId(), "새 카드", "내용");
            given(kanbanColumnRepository.findById(column.getId())).willReturn(Optional.of(column));
            given(userRepository.findById(otherUserId)).willReturn(Optional.of(User.builder().id(otherUserId).build()));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.createCard(column.getId(), otherUserId, request));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_COLUMN);
        }
    }

    @Nested
    @DisplayName("카드 조회")
    class GetCard {
        @Test
        @DisplayName("성공")
        void getCard_success() {
            // given
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when
            Card foundCard = cardService.getCard(card.getId(), user.getId());

            // then
            assertNotNull(foundCard);
            assertEquals(card.getId(), foundCard.getId());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void getCard_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.getCard(card.getId(), otherUserId));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_CARD);
        }
    }

    @Nested
    @DisplayName("카드 수정")
    class UpdateCard {
        @Test
        @DisplayName("성공")
        void updateCard_success() {
            // given
            CardUpdateRequest request = new CardUpdateRequest("수정된 제목", "수정된 내용");
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when
            Card updatedCard = cardService.updateCard(card.getId(), user.getId(), request);

            // then
            assertEquals(request.getTitle(), updatedCard.getTitle());
            assertEquals(request.getContent(), updatedCard.getContent());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void updateCard_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
            CardUpdateRequest request = new CardUpdateRequest("수정된 제목", "수정된 내용");
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.updateCard(card.getId(), otherUserId, request));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_CARD);
        }
    }

    @Nested
    @DisplayName("카드 이동")
    class ShiftCard {
        @Test
        @DisplayName("성공")
        void shiftCard_success() {
            // given
            KanbanColumn newColumn = KanbanColumn.builder().id(2L).board(board).build();
            Integer newPosition = 1;
            cardKanbanColumn = column; // 초기 상태 설정
            cardPosition = 1; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(kanbanColumnRepository.findById(newColumn.getId())).willReturn(Optional.of(newColumn));

            // when
            cardService.shiftCard(card.getId(), user.getId(), new CardShiftRequest(newColumn.getId(), newPosition));

            // then
            verify(cardRepository).decrementPositionsAfter(column.getId(), card.getPosition());
            verify(cardRepository).incrementPositionsFrom(newColumn.getId(), newPosition);
            assertThat(card.getKanbanColumn()).isEqualTo(newColumn);
            assertThat(card.getPosition()).isEqualTo(newPosition);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void shiftCard_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
            KanbanColumn newColumn = KanbanColumn.builder().id(2L).board(board).build();
            Integer newPosition = 1;
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.shiftCard(card.getId(), otherUserId, new CardShiftRequest(newColumn.getId(), newPosition)));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_CARD);
        }
    }

    @Nested
    @DisplayName("카드 삭제")
    class DeleteCard {
        @Test
        @DisplayName("성공")
        void deleteCard_success() {
            // given
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            doNothing().when(cardRepository).delete(card);

            // when & then
            assertDoesNotThrow(() -> cardService.deleteCard(card.getId(), user.getId()));
            verify(cardRepository).delete(card);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void deleteCard_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.deleteCard(card.getId(), otherUserId));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_CARD);
        }
    }

    @Nested
    @DisplayName("태그 관리")
    class TagManagement {
        @Test
        @DisplayName("태그 추가 성공")
        void addTagToCard_success() {
            // given
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));
            doAnswer(invocation -> {
                CardTag cardTag = new gon.til.domain.entity.CardTag(card, tag);
                card.getCardTags().add(cardTag);
                return null;
            }).when(card).addTag(any(Tag.class));

            // when
            Card resultCard = cardService.addTagToCard(card.getId(), tag.getId(), user.getId());

            // then
            assertThat(resultCard.getCardTags()).hasSize(1);
            assertThat(resultCard.getCardTags().iterator().next().getTag()).isEqualTo(tag);
        }

        @Test
        @DisplayName("태그 추가 실패 - 카드 권한 없음")
        void addTagToCard_fail_cardAccessDenied() {
            // given
            Long otherUserId = 99L;
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.addTagToCard(card.getId(), tag.getId(), otherUserId));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_CARD);
        }

        @Test
        @DisplayName("태그 추가 실패 - 태그 권한 없음")
        void addTagToCard_fail_tagAccessDenied() {
            // given
            Long otherUserId = 99L;
            Tag otherUserTag = Tag.builder().id(2L).project(Project.builder().user(User.builder().id(otherUserId).build()).build()).name("Other User Tag").build();

            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(tagRepository.findById(otherUserTag.getId())).willReturn(Optional.of(otherUserTag));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.addTagToCard(card.getId(), otherUserTag.getId(), user.getId()));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_TAG);
        }

        @Test
        @DisplayName("태그 추가 실패 - 태그가 다른 프로젝트에 속함")
        void addTagToCard_fail_tagNotInSameProject() {
            // given
            Project otherProject = Project.builder().id(2L).user(user).build();
            Tag tagInOtherProject = Tag.builder().id(2L).project(otherProject).name("Other Project Tag").build();

            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(tagRepository.findById(tagInOtherProject.getId())).willReturn(Optional.of(tagInOtherProject));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.addTagToCard(card.getId(), tagInOtherProject.getId(), user.getId()));
                        assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.TAG_NOT_IN_SAME_PROJECT);
        }

        @Test
        @DisplayName("태그 삭제 성공")
        void removeTagFromCard_success() {
            // given
            given(card.getId()).willReturn(1L);
            given(card.getKanbanColumn()).willReturn(column);
            given(card.getUser()).willReturn(user);
            doAnswer(invocation -> {
                CardTag cardTag = new gon.til.domain.entity.CardTag(card, tag);
                cardTagsSet.add(cardTag);
                return null;
            }).when(card).addTag(any(Tag.class));
            doAnswer(invocation -> {
                Long tagIdToRemove = invocation.getArgument(0);
                cardTagsSet.removeIf(cardTag -> cardTag.getTag().getId().equals(tagIdToRemove));
                return null;
            }).when(card).removeTag(any(Long.class));
            card.addTag(tag); // 테스트를 위해 미리 태그 추가
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));

            // when
            cardService.removeTagFromCard(card.getId(), tag.getId(), user.getId());

            // then
            assertThat(cardTagsSet).isEmpty();
        }

        @Test
        @DisplayName("태그 삭제 실패 - 카드 권한 없음")
        void removeTagFromCard_fail_cardAccessDenied() {
            // given
            Long otherUserId = 99L;
            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.removeTagFromCard(card.getId(), tag.getId(), otherUserId));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_CARD);
        }

        @Test
        @DisplayName("태그 삭제 실패 - 태그 권한 없음")
        void removeTagFromCard_fail_tagAccessDenied() {
            // given
            Long otherUserId = 99L;
            Tag otherUserTag = Tag.builder().id(2L).project(Project.builder().user(User.builder().id(otherUserId).build()).build()).name("Other User Tag").build();

            cardKanbanColumn = column; // 초기 상태 설정
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(tagRepository.findById(otherUserTag.getId())).willReturn(Optional.of(otherUserTag));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.removeTagFromCard(card.getId(), otherUserTag.getId(), user.getId()));
            assertThat(exception.getGlobalErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_TAG);
        }
    }
}

