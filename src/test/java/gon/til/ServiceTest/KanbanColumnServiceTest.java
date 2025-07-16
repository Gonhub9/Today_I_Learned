package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.KanbanColumn;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.BoardRepository;
import gon.til.domain.repository.KanbanColumnRepository;
import gon.til.domain.service.KanbanColumnService;
import gon.til.global.exception.GlobalException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("KanbanColumnService 테스트")
class KanbanColumnServiceTest {

    @Mock
    private KanbanColumnRepository kanbanColumnRepository;

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private KanbanColumnService kanbanColumnService;

    private User user;
    private Project project;
    private Board board;
    private List<KanbanColumn> columns;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        project = Project.builder().id(1L).user(user).build();
        board = Board.builder().id(1L).project(project).build();

        // 테스트용 컬럼 리스트 초기화
        columns = new ArrayList<>();
        columns.add(KanbanColumn.builder().id(1L).title("To Do").position(1).board(board).build());
        columns.add(KanbanColumn.builder().id(2L).title("In Progress").position(2).board(board).build());
        columns.add(KanbanColumn.builder().id(3L).title("Done").position(3).board(board).build());
    }

    @Test
    @DisplayName("컬럼 생성 성공")
    void createColumn_success() {
        // given
        String newTitle = "New Column";
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
        given(kanbanColumnRepository.existsByBoardIdAndTitle(board.getId(), newTitle)).willReturn(false);
        given(kanbanColumnRepository.findByBoardIdOrderByPosition(board.getId())).willReturn(columns);
        given(kanbanColumnRepository.save(any(KanbanColumn.class))).willAnswer(invocation -> {
            KanbanColumn savedColumn = invocation.getArgument(0);
            // 실제 id를 할당하는 것처럼 흉내
            return KanbanColumn.builder()
                .id(4L)
                .title(savedColumn.getTitle())
                .position(savedColumn.getPosition())
                .board(savedColumn.getBoard())
                .build();
        });

        // when
        KanbanColumn newColumn = kanbanColumnService.createColumn(board.getId(), user.getId(), newTitle);

        // then
        assertThat(newColumn.getTitle()).isEqualTo(newTitle);
        assertThat(newColumn.getPosition()).isEqualTo(columns.size() + 1);
        verify(kanbanColumnRepository).save(any(KanbanColumn.class));
    }

    @Test
    @DisplayName("컬럼 삭제 및 순서 재정렬 성공")
    void deleteColumn_success() {
        // given
        Long columnToDeleteId = 2L; // "In Progress" 컬럼
        KanbanColumn columnToDelete = columns.get(1);

        given(kanbanColumnRepository.findById(columnToDeleteId)).willReturn(Optional.of(columnToDelete));
        doNothing().when(kanbanColumnRepository).delete(columnToDelete);

        // 삭제 후 남을 컬럼들 (reorderPositions가 호출될 때 반환될 리스트)
        List<KanbanColumn> remainingColumns = new ArrayList<>();
        remainingColumns.add(columns.get(0)); // To Do
        remainingColumns.add(columns.get(2)); // Done
        given(kanbanColumnRepository.findByBoardIdOrderByPosition(board.getId())).willReturn(remainingColumns);


        // when
        kanbanColumnService.deleteColumn(columnToDeleteId, user.getId());

        // then
        // 1. delete 메서드가 호출되었는지 검증
        verify(kanbanColumnRepository).delete(columnToDelete);
        
        // 2. reorderPositions 내부의 findByBoardIdOrderByPosition가 호출되었는지 검증
        verify(kanbanColumnRepository, times(1)).findByBoardIdOrderByPosition(board.getId());

        // 3. 남은 컬럼들의 position이 1과 2로 재정렬되었는지 검증
        assertThat(remainingColumns.get(0).getPosition()).isEqualTo(1);
        assertThat(remainingColumns.get(1).getPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("컬럼 순서 변경 성공")
    void updateColumnPositions_success() {
        // given
        List<Long> newOrderIds = List.of(3L, 1L, 2L); // Done -> To Do -> In Progress
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
        given(kanbanColumnRepository.findAllByIdIn(newOrderIds)).willReturn(columns);

        // when
        List<KanbanColumn> updatedColumns = kanbanColumnService.updateColumnPositions(board.getId(), user.getId(), newOrderIds);

        // then
        assertThat(updatedColumns).hasSize(3);
        // 정렬된 결과에서 각 컬럼의 ID와 새로운 position을 확인
        assertThat(updatedColumns.get(0).getId()).isEqualTo(3L);
        assertThat(updatedColumns.get(0).getPosition()).isEqualTo(1);
        assertThat(updatedColumns.get(1).getId()).isEqualTo(1L);
        assertThat(updatedColumns.get(1).getPosition()).isEqualTo(2);
        assertThat(updatedColumns.get(2).getId()).isEqualTo(2L);
        assertThat(updatedColumns.get(2).getPosition()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("기본 컬럼 생성 성공")
    void createDefaultColumns_success() {
        // given
        // createColumn 내부에서 호출되는 메서드들을 mock
        given(boardRepository.findById(anyLong())).willReturn(Optional.of(board));
        given(kanbanColumnRepository.existsByBoardIdAndTitle(anyLong(), anyString())).willReturn(false);
        // getNextPosition이 호출될 때마다 다른 값을 반환하도록 설정
        given(kanbanColumnRepository.findByBoardIdOrderByPosition(anyLong()))
            .willReturn(new ArrayList<>()) // 첫번째 호출
            .willReturn(List.of(KanbanColumn.builder().build())) // 두번째 호출
            .willReturn(List.of(KanbanColumn.builder().build(), KanbanColumn.builder().build())) // 세번째 호출
            .willReturn(List.of(KanbanColumn.builder().build(), KanbanColumn.builder().build(), KanbanColumn.builder().build())); // 네번째 호출

        // when
        kanbanColumnService.createDefaultColumns(board);

        // then
        // save가 4번 호출되었는지 검증
        verify(kanbanColumnRepository, times(4)).save(any(KanbanColumn.class));
    }

    @Test
    @DisplayName("컬럼 삭제 실패 - 다른 사용자")
    void deleteColumn_fail_accessDenied() {
        // given
        Long otherUserId = 99L;
        KanbanColumn columnToDelete = columns.get(0);
        given(kanbanColumnRepository.findById(columnToDelete.getId())).willReturn(Optional.of(columnToDelete));

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            kanbanColumnService.deleteColumn(columnToDelete.getId(), otherUserId);
        });
        
        assertThat(exception.getMessage()).isEqualTo("컬럼 접근 권한이 없습니다.");
    }
}
