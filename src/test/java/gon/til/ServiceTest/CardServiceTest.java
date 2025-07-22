package gon.til.ServiceTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.Card;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService 테스트")
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
    private Card card;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        project = Project.builder().id(1L).user(user).build();
        board = Board.builder().id(1L).project(project).build();
        column = KanbanColumn.builder().id(1L).board(board).build();
        card = Card.builder()
            .id(1L)
            .title("Test Card")
            .content("Test Content")
            .user(user)
            .kanbanColumn(column)
            .build();
    }

    @Nested
    @DisplayName("카드 생성 테스트")
    class CreateCardTest {

        @Test
        @DisplayName("성공")
        void createCard_success() {
            // given
            when(kanbanColumnRepository.findById(anyLong())).thenReturn(Optional.of(column));
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
            when(cardRepository.save(any(Card.class))).thenReturn(card);

            // when
            Card newCard = cardService.createCard(1L, 1L, "Test Card", "Test Content");

            // then
            assertNotNull(newCard);
            assertEquals("Test Card", newCard.getTitle());
            verify(cardRepository, times(1)).save(any(Card.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 컬럼")
        void createCard_fail_columnNotFound() {
            // given
            when(kanbanColumnRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.createCard(1L, 1L, "Test", "Content"));
            assertEquals(GlobalErrorCode.NOT_FOUND_COLUMN, exception.getGlobalErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void createCard_fail_accessDenied() {
            // given
            User otherUser = User.builder().id(2L).build();
            when(kanbanColumnRepository.findById(anyLong())).thenReturn(Optional.of(column));
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.createCard(1L, 2L, "Test", "Content"));
            assertEquals(GlobalErrorCode.ACCESS_DENIED_COLUMN, exception.getGlobalErrorCode());
        }
    }

    @Nested
    @DisplayName("카드 조회 테스트")
    class GetCardTest {

        @Test
        @DisplayName("성공")
        void getCard_success() {
            // given
            when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

            // when
            Card foundCard = cardService.getCard(1L, 1L);

            // then
            assertNotNull(foundCard);
            assertEquals(card.getId(), foundCard.getId());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카드")
        void getCard_fail_cardNotFound() {
            // given
            when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.getCard(1L, 1L));
            assertEquals(GlobalErrorCode.NOT_FOUND_CARD, exception.getGlobalErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void getCard_fail_accessDenied() {
            // given
            when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () ->
                cardService.getCard(1L, 2L)); // 다른 사용자 ID
            assertEquals(GlobalErrorCode.ACCESS_DENIED_CARD, exception.getGlobalErrorCode());
        }
    }

    @Nested
    @DisplayName("카드 수정 테스트")
    class UpdateCardTest {

        @Test
        @DisplayName("성공")
        void updateCard_success() {
            // given
            when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));
            String newTitle = "Updated Title";
            String newContent = "Updated Content";

            // when
            Card updatedCard = cardService.updateCard(1L, 1L, newTitle, newContent);

            // then
            assertNotNull(updatedCard);
            assertEquals(newTitle, updatedCard.getTitle());
            assertEquals(newContent, updatedCard.getContent());
        }
    }

    @Nested
    @DisplayName("카드 삭제 테스트")
    class DeleteCardTest {

        @Test
        @DisplayName("성공")
        void deleteCard_success() {
            // given
            when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));
            doNothing().when(cardRepository).delete(any(Card.class));

            // when & then
            assertDoesNotThrow(() -> cardService.deleteCard(1L));
            verify(cardRepository, times(1)).delete(card);
        }
    }

    @Nested
    @DisplayName("카드에 태그 추가/삭제 테스트")
    class TagManagementTest {

        private Tag tag;

        @BeforeEach
        void tagSetUp() {
            tag = Tag.builder().id(1L).name("Test Tag").build();
        }

        @Test
        @DisplayName("태그 추가 성공")
        void addTagToCard_success() {
            // given
            when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));
            when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));

            // when
            Card resultCard = cardService.addTagToCard(1L, 1L);

            // then
            assertTrue(resultCard.getCardTags().stream()
                .anyMatch(cardTag -> cardTag.getTag().equals(tag)));
        }

        @Test
        @DisplayName("태그 삭제 성공")
        void removeTagFromCard_success() {
            // given
            card.addTag(tag); // 먼저 태그를 추가해둠
            when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

            // when
            cardService.removeTagFromCard(1L, 1L);

            // then
            assertTrue(card.getCardTags().isEmpty());
        }
    }
}