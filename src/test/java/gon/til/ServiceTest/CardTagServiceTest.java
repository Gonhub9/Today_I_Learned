package gon.til.ServiceTest;

import gon.til.domain.dto.card.CardResponse;
import gon.til.domain.entity.*;
import gon.til.domain.repository.CardRepository;
import gon.til.domain.repository.TagRepository;
import gon.til.domain.service.CardTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardTagService 테스트")
class CardTagServiceTest {

    @InjectMocks
    private CardTagService cardTagService;

    @Mock
    private CardRepository cardRepository;
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
        // 테스트 메소드 내에서 cardTags를 조작하기 위해 초기화
        card = Card.builder()
                .id(1L)
                .title("Test Card")
                .content("Test Content")
                .user(user)
                .kanbanColumn(column)
                .project(project)
                .position(1)
                .build();
    }

    @Nested
    @DisplayName("태그 관리")
    class TagManagement {

        @Test
        @DisplayName("카드에 태그 추가 성공")
        void addTagToCard_success() {
            // given
            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));

            // when
            CardResponse resultCard = cardTagService.addTagToCard(card.getId(), tag.getId(), user.getId());

            // then
            // CardResponse.from() 메소드가 card.getCardTags()를 사용하므로,
            // 실제 card 객체의 cardTags set에 tag가 추가되었는지 확인하는 것과 같다.
            assertThat(card.getCardTags()).hasSize(1);
            assertThat(resultCard.getTags()).hasSize(1);
            assertThat(resultCard.getTags().get(0).getName()).isEqualTo(tag.getName());
        }

        @Test
        @DisplayName("카드에서 태그 삭제 성공")
        void removeTagFromCard_success() {
            // given
            // 먼저 카드에 태그를 추가해놓은 상태로 시작
            card.addTag(tag);
            assertThat(card.getCardTags()).hasSize(1);

            given(cardRepository.findById(card.getId())).willReturn(Optional.of(card));
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));

            // when
            cardTagService.removeTagFromCard(card.getId(), tag.getId(), user.getId());

            // then
            // card.removeTag가 호출되어 cardTags Set의 크기가 0이 되어야 한다.
            assertThat(card.getCardTags()).isEmpty();
        }
    }
}
