package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import gon.til.domain.dto.card.CardCreateRequest;
import gon.til.domain.dto.card.CardResponse;
import gon.til.domain.dto.card.CardShiftRequest;
import gon.til.domain.dto.card.CardUpdateRequest;
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
import gon.til.domain.service.CardTagService;
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
    private CardTagService cardTagService;

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
    private Tag tag;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).displayName("testuser").build();
        project = Project.builder().id(1L).user(user).build();
        board = Board.builder().id(1L).project(project).build();
        column = KanbanColumn.builder().id(1L).board(board).position(1).build();
        tag = Tag.builder().id(1L).project(project).name("Test Tag").build();
        card = Card.builder()
            .id(1L)
            .title("Test Card")
            .content("Test Content")
            .user(user)
            .kanbanColumn(column)
            .position(1)
            .build();
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
            given(cardRepository.save(any(Card.class))).willAnswer(i -> {
                Card argument = i.getArgument(0);
                return Card.builder()
                    .id(2L)
                    .title(argument.getTitle())
                    .content(argument.getContent())
                    .user(argument.getUser())
                    .kanbanColumn(argument.getKanbanColumn())
                    .build();
            });

            // when
            CardResponse newCardResponse = cardService.createCard(column.getId(), user.getId(), request);

            // then
            assertNotNull(newCardResponse);
            assertEquals(request.getTitle(), newCardResponse.getTitle());
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
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when
            CardResponse foundCard = cardService.getCard(card.getId(), user.getId());

            // then
            assertNotNull(foundCard);
            assertEquals(card.getId(), foundCard.getId());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void getCard_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
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
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));

            // when
            CardResponse updatedCard = cardService.updateCard(card.getId(), user.getId(), request);

            // then
            assertEquals(request.getTitle(), updatedCard.getTitle());
            assertEquals(request.getContent(), updatedCard.getContent());
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
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(kanbanColumnRepository.findById(newColumn.getId())).willReturn(Optional.of(newColumn));

            // when
            CardResponse shiftedCard = cardService.shiftCard(card.getId(), user.getId(), new CardShiftRequest(newColumn.getId(), newPosition));

            // then
            verify(cardRepository).decrementPositionsAfter(column.getId(), card.getPosition());
            verify(cardRepository).incrementPositionsFrom(newColumn.getId(), newPosition);
                        assertThat(shiftedCard.getColumnId()).isEqualTo(newColumn.getId());
            assertThat(shiftedCard.getPosition()).isEqualTo(newPosition);
        }
    }

    
}

