package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import gon.til.domain.dto.board.BoardCreateRequest;
import gon.til.domain.dto.board.BoardUpdateRequest;
import gon.til.domain.entity.Board;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.BoardRepository;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.service.BoardService;
import gon.til.domain.service.KanbanColumnService;
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
@DisplayName("BoardService 테스트")
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private KanbanColumnService kanbanColumnService;

    @InjectMocks
    private BoardService boardService;

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        project = Project.builder().id(1L).user(user).build();
    }

    @Nested
    @DisplayName("보드 생성")
    class CreateBoard {
        @Test
        @DisplayName("성공 - 기본 컬럼과 함께 생성")
        void createBoard_Success() {
            // Given
            BoardCreateRequest request = new BoardCreateRequest(project.getId(), "새 보드");
            given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
            given(boardRepository.existsByProjectId(project.getId())).willReturn(false);
            given(boardRepository.save(any(Board.class))).willAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(kanbanColumnService).createDefaultColumns(any(Board.class));

            // When
            Board createdBoard = boardService.createBoard(project.getId(), request);

            // Then
            assertThat(createdBoard.getTitle()).isEqualTo(request.getTitle());
            assertThat(createdBoard.getProject()).isEqualTo(project);
            // 기본 컬럼 생성 메소드가 호출되었는지 검증
            verify(kanbanColumnService).createDefaultColumns(createdBoard);
        }

        @Test
        @DisplayName("실패 - 이미 보드가 존재")
        void createBoard_Duplicate_Fail() {
            // Given
            BoardCreateRequest request = new BoardCreateRequest(project.getId(), "새 보드");
            given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
            given(boardRepository.existsByProjectId(project.getId())).willReturn(true);

            // When & Then
            assertThrows(GlobalException.class, () -> boardService.createBoard(project.getId(), request));
        }
    }

    @Nested
    @DisplayName("보드 수정")
    class UpdateBoard {
        @Test
        @DisplayName("성공")
        void updateBoard_Success() {
            // Given
            Board board = Board.builder().id(1L).project(project).title("원본 제목").build();
            BoardUpdateRequest request = new BoardUpdateRequest("수정된 제목");
            given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

            // When
            Board updatedBoard = boardService.updateBoardTitle(board.getId(), user.getId(), request);

            // Then
            assertThat(updatedBoard.getTitle()).isEqualTo(request.getTitle());
        }

        @Test
        @DisplayName("실패 - 다른 사용자")
        void updateBoard_AccessDenied_Fail() {
            // Given
            Long otherUserId = 99L;
            Board board = Board.builder().id(1L).project(project).title("원본 제목").build();
            BoardUpdateRequest request = new BoardUpdateRequest("수정된 제목");
            given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

            // When & Then
            // When & Then
            // ACCESS_DENIED_BOARD는 403 Forbidden 에러에 해당하며, 보드 접근 권한이 없을 때 발생합니다.
            GlobalException exception = assertThrows(GlobalException.class, () -> boardService.updateBoardTitle(board.getId(), otherUserId, request));
            System.out.println("Actual Error Code: " + exception.getErrorCode()); // 디버깅을 위한 출력
            assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_BOARD.getCode());
        }
    }
}
