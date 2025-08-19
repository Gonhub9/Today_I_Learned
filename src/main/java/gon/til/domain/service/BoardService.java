package gon.til.domain.service;

import gon.til.domain.dto.board.BoardCreateRequest;
import gon.til.domain.dto.board.BoardResponse;
import gon.til.domain.dto.board.BoardUpdateRequest;
import gon.til.domain.entity.Board;
import gon.til.domain.entity.Project;
import gon.til.domain.repository.BoardRepository;
import gon.til.domain.repository.ProjectRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final ProjectRepository projectRepository;
    private final KanbanColumnService kanbanColumnService;

    /**
     * 프로젝트에 대한 보드 생성
     * - 프로젝트당 1개의 보드만 생성 가능
     * - 이미 보드가 있으면 예외 발생
     * - 보드 생성 시 기본 컬럼(할 일, 진행 중, 완료, 복습 필요) 자동 생성
     */
    @Transactional
    public Board createBoard(Long projectId, BoardCreateRequest request) {

        // 1. 프로젝트가 있는지 확인 및 조회
        Project project = getProjectById(projectId);

        // 2. 중복 보드 확인
        validateDuplicateBoard(projectId);

        // 3. 보드 생성
        Board board = new Board(request.getTitle(), project);
        Board savedBoard = boardRepository.save(board);

        // 4. 기본 컬럼 생성
        kanbanColumnService.createDefaultColumns(savedBoard);

        return savedBoard;
    }

    /**
     * 프로젝트의 보드 조회
     * - 프로젝트에 연결된 보드 반환
     * - 보드가 없으면 예외 발생
     */
    public Board getBoardByProject(Long projectId, Long boardId) {

        // 보드 조회
        return boardRepository.findByProjectIdAndBoardId(projectId, boardId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_BOARD));

    }

    public List<BoardResponse> findAllBoards() {

        // 보드 전체 조회
        List<Board> boards = boardRepository.findAll();

        // Board 엔티티 리스트를 DTO로 변환
        // 데이터가 없으면 비어있는 리스트가 반환
        return boards.stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());

    }

    /**
     * 보드 제목 수정
     * - 보드 소유자인지 확인 (프로젝트 소유자 = 보드 소유자)
     */
    @Transactional
    public Board updateBoardTitle(Long boardId, Long userId, BoardUpdateRequest request) {

        // 1. 보드 존재 확인
        Board board = getBoardById(boardId);

        // 2. 보드 소유권 확인 (보드 → 프로젝트 → 사용자)
        validateBoardOwnership(board, userId);

        // 3. 제목 수정
        board.updateBoard(request.getTitle());

        return board;

    }

    /**
     * 보드 삭제
     * - 프로젝트 삭제 시 자동으로 삭제되므로 직접 삭제는 제한적
     * - 권한 확인 필요
     */
    @Transactional
    public void deleteBoard(Long boardId, Long userId) {

        // 1. 보드 존재 확인
        Board board = getBoardById(boardId);

        // 2. 보드 소유권 확인
        validateBoardOwnership(board, userId);

        // 3. 보드 삭제 (연관된 컬럼, 카드도 cascade로 자동 삭제)
        boardRepository.delete(board);

    }

    // ===== private 헬퍼 메서드들 =====

    /**
     * 프로젝트 존재 확인 및 조회
     */
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_PROJECT));
    }

    /**
     * 보드 존재 확인 및 조회
     */
    private Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_BOARD));
    }

    /**
     * 보드 소유권 확인
     * - 보드의 프로젝트 소유자와 userId 비교
     */
    private void validateBoardOwnership(Board board, Long userId) {
        // Board -> Project -> User 연관 관계
        Long projectOwnerId = board.getProject().getUser().getId();

        if (!projectOwnerId.equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_BOARD);
        }
    }

    /**
     * 중복 보드 확인
     * - 프로젝트당 1개의 보드만 허용
     */
    private void validateDuplicateBoard(Long projectId) {
        if (boardRepository.existsByProjectId(projectId)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_BOARD);
        }
    }
}