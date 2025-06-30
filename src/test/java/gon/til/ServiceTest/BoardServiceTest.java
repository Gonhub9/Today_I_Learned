package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gon.til.TilApplication;
import gon.til.domain.entity.Board;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import gon.til.domain.repository.BoardRepository;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.UserRepository;
import gon.til.domain.service.BoardService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TilApplication.class)
@Transactional
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BoardRepository boardRepository;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User("testuser", "test@test.com", "password123");
        testUser = userRepository.save(testUser);

        // 테스트 프로젝트 생성
        testProject = new Project("Test Project", "Test Description", "Backend", testUser);
        testProject = projectRepository.save(testProject);
    }

    @Test
    @DisplayName("보드 생성 성공 테스트")
    void createBoard_Success() {
        // Given
        String boardTitle = "Test Board";

        // When
        Board createdBoard = boardService.createBoard(testProject.getId(), boardTitle);

        // Then
        assertThat(createdBoard.getId()).isNotNull();
        assertThat(createdBoard.getTitle()).isEqualTo(boardTitle);
        assertThat(createdBoard.getProject().getId()).isEqualTo(testProject.getId());
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트로 보드 생성 실패 테스트")
    void createBoard_ProjectNotFound_ThrowsException() {
        // Given
        Long nonExistentProjectId = 999L;
        String boardTitle = "Test Board";

        // When & Then
        assertThatThrownBy(() ->
                boardService.createBoard(nonExistentProjectId, boardTitle)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_PROJECT);
    }

    @Test
    @DisplayName("중복 보드 생성 실패 테스트")
    void createBoard_DuplicateBoard_ThrowsException() {
        // Given
        String boardTitle1 = "First Board";
        String boardTitle2 = "Second Board";

        // 첫 번째 보드 생성
        boardService.createBoard(testProject.getId(), boardTitle1);

        // When & Then
        assertThatThrownBy(() ->
                boardService.createBoard(testProject.getId(), boardTitle2)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_BOARD);
    }

    @Test
    @DisplayName("프로젝트별 보드 조회 성공 테스트")
    void getBoardByProject_Success() {
        // Given
        String boardTitle = "Test Board";
        Board createdBoard = boardService.createBoard(testProject.getId(), boardTitle);

        // When
        Board foundBoard = boardService.getBoardByProject(testProject.getId());

        // Then
        assertThat(foundBoard.getId()).isEqualTo(createdBoard.getId());
        assertThat(foundBoard.getTitle()).isEqualTo(boardTitle);
        assertThat(foundBoard.getProject().getId()).isEqualTo(testProject.getId());
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트의 보드 조회 실패 테스트")
    void getBoardByProject_ProjectNotFound_ThrowsException() {
        // Given
        Long nonExistentProjectId = 999L;

        // When & Then
        assertThatThrownBy(() ->
                boardService.getBoardByProject(nonExistentProjectId)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_PROJECT);
    }

    @Test
    @DisplayName("보드가 없는 프로젝트 조회 실패 테스트")
    void getBoardByProject_BoardNotFound_ThrowsException() {
        // Given
        // testProject는 있지만 보드는 생성하지 않음

        // When & Then
        assertThatThrownBy(() ->
                boardService.getBoardByProject(testProject.getId())
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_BOARD);
    }

    @Test
    @DisplayName("보드 제목 수정 성공 테스트")
    void updateBoardTitle_Success() {
        // Given
        String originalTitle = "Original Board";
        String newTitle = "Updated Board";
        Board createdBoard = boardService.createBoard(testProject.getId(), originalTitle);

        // When
        Board updatedBoard = boardService.updateBoardTitle(
                createdBoard.getId(), testUser.getId(), newTitle);

        // Then
        assertThat(updatedBoard.getId()).isEqualTo(createdBoard.getId());
        assertThat(updatedBoard.getTitle()).isEqualTo(newTitle);
    }

    @Test
    @DisplayName("존재하지 않는 보드 제목 수정 실패 테스트")
    void updateBoardTitle_BoardNotFound_ThrowsException() {
        // Given
        Long nonExistentBoardId = 999L;
        String newTitle = "New Title";

        // When & Then
        assertThatThrownBy(() ->
                boardService.updateBoardTitle(nonExistentBoardId, testUser.getId(), newTitle)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_BOARD);
    }

    @Test
    @DisplayName("다른 사용자의 보드 제목 수정 실패 테스트")
    void updateBoardTitle_AccessDenied_ThrowsException() {
        // Given
        User tempUser = new User("anotheruser", "another@test.com", "password");
        User anotherUser = userRepository.save(tempUser);

        String originalTitle = "Original Board";
        String newTitle = "Updated Board";
        Board createdBoard = boardService.createBoard(testProject.getId(), originalTitle);

        // When & Then
        assertThatThrownBy(() ->
                boardService.updateBoardTitle(createdBoard.getId(), anotherUser.getId(), newTitle)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.ACCESS_DENIED_BOARD);
    }

    @Test
    @DisplayName("보드 삭제 성공 테스트")
    void deleteBoard_Success() {
        // Given
        String boardTitle = "Test Board";
        Board createdBoard = boardService.createBoard(testProject.getId(), boardTitle);

        // When
        boardService.deleteBoard(createdBoard.getId(), testUser.getId());

        // Then
        assertThat(boardRepository.findById(createdBoard.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 보드 삭제 실패 테스트")
    void deleteBoard_BoardNotFound_ThrowsException() {
        // Given
        Long nonExistentBoardId = 999L;

        // When & Then
        assertThatThrownBy(() ->
                boardService.deleteBoard(nonExistentBoardId, testUser.getId())
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.NOT_FOUND_BOARD);
    }

    @Test
    @DisplayName("다른 사용자의 보드 삭제 실패 테스트")
    void deleteBoard_AccessDenied_ThrowsException() {
        // Given
        User tempUser = new User("anotheruser", "another@test.com", "password");
        User anotherUser = userRepository.save(tempUser);

        String boardTitle = "Test Board";
        Board createdBoard = boardService.createBoard(testProject.getId(), boardTitle);

        // When & Then
        assertThatThrownBy(() ->
                boardService.deleteBoard(createdBoard.getId(), anotherUser.getId())
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.ACCESS_DENIED_BOARD);
    }

    @Test
    @DisplayName("프로젝트당 1개 보드 제한 테스트")
    void createBoard_OnePerProject_Limitation() {
        // Given
        String boardTitle1 = "First Board";
        String boardTitle2 = "Second Board";

        // When
        Board firstBoard = boardService.createBoard(testProject.getId(), boardTitle1);

        // Then
        assertThat(firstBoard).isNotNull();
        assertThat(firstBoard.getTitle()).isEqualTo(boardTitle1);

        // 같은 프로젝트에 두 번째 보드 생성 시도 시 실패
        assertThatThrownBy(() ->
                boardService.createBoard(testProject.getId(), boardTitle2)
        )
                .isInstanceOf(GlobalException.class)
                .extracting("globalErrorCode")
                .isEqualTo(GlobalErrorCode.DUPLICATE_BOARD);
    }

    @Test
    @DisplayName("다른 프로젝트에는 각각 보드 생성 가능 테스트")
    void createBoard_DifferentProjects_Success() {
        // Given
        Project anotherProject = new Project("Another Project", "Description", "Frontend", testUser);
        anotherProject = projectRepository.save(anotherProject);

        String boardTitle1 = "Project1 Board";
        String boardTitle2 = "Project2 Board";

        // When
        Board board1 = boardService.createBoard(testProject.getId(), boardTitle1);
        Board board2 = boardService.createBoard(anotherProject.getId(), boardTitle2);

        // Then
        assertThat(board1).isNotNull();
        assertThat(board2).isNotNull();
        assertThat(board1.getProject().getId()).isEqualTo(testProject.getId());
        assertThat(board2.getProject().getId()).isEqualTo(anotherProject.getId());
    }
}