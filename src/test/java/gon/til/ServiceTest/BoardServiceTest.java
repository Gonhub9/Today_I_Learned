package gon.til.ServiceTest;

import gon.til.domain.dto.board.BoardResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

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
    private Board board;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        project = Project.builder().id(1L).user(user).build();
        board = Board.builder().id(1L).project(project).title("원본 제목").build();
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
            given(boardRepository.save(any(Board.class))).willAnswer(invocation -> {
                Board savedBoard = invocation.getArgument(0);
                // Simulate saving by setting an ID
                return Board.builder()
                    .id(2L)
                    .title(savedBoard.getTitle())
                    .project(savedBoard.getProject())
                    .build();
            });
            doNothing().when(kanbanColumnService).createDefaultColumns(any(Board.class));

            // When
            BoardResponse createdBoardResponse = boardService.createBoard(project.getId(), user.getId(), request);

            // Then
            assertThat(createdBoardResponse.getTitle()).isEqualTo(request.getTitle());
            assertThat(createdBoardResponse.getProjectId()).isEqualTo(project.getId());
            // 기본 컬럼 생성 메소드가 호출되었는지 검증
            verify(kanbanColumnService).createDefaultColumns(any(Board.class));
        }

        @Test
        @DisplayName("실패 - 이미 보드가 존재")
        void createBoard_Duplicate_Fail() {
            // Given
            BoardCreateRequest request = new BoardCreateRequest(project.getId(), "새 보드");
            given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
            given(boardRepository.existsByProjectId(project.getId())).willReturn(true);

            // When & Then
            assertThrows(GlobalException.class, () -> boardService.createBoard(project.getId(), user.getId(), request));
        }
    }

    @Nested
    @DisplayName("보드 수정")
    class UpdateBoard {
        @Test
        @DisplayName("성공")
        void updateBoard_Success() {
            // Given
            BoardUpdateRequest request = new BoardUpdateRequest("수정된 제목");
            given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

            // When
            BoardResponse updatedBoardResponse = boardService.updateBoardTitle(board.getId(), user.getId(), request);

            // Then
            assertThat(updatedBoardResponse.getTitle()).isEqualTo(request.getTitle());
        }

        @Test
        @DisplayName("실패 - 다른 사용자")
        void updateBoard_AccessDenied_Fail() {
            // Given
            Long otherUserId = 99L;
            BoardUpdateRequest request = new BoardUpdateRequest("수정된 제목");
            given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));

            // When & Then
            GlobalException exception = assertThrows(GlobalException.class, () -> boardService.updateBoardTitle(board.getId(), otherUserId, request));
            assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_BOARD.getCode());
        }
    }
}