package gon.til.domain.service;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.Project;
import gon.til.domain.repository.BoardRepository;
import gon.til.domain.repository.ProjectRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final ProjectRepository projectRepository;

    /**
     * 프로젝트에 대한 보드 생성
     * - 프로젝트당 1개의 보드만 생성 가능
     * - 이미 보드가 있으면 예외 발생
     */
    public Board createBoard(Long projectId, String title) {

        // 1. 프로젝트가 있는지 확인 및 조회
        Project project = getProjectById(projectId);

        // 2. 중복 보드 확인
        validateDuplicateBoard(projectId);

        // 3. 보드 생성
        Board board = new Board(title, project);
        return boardRepository.save(board);
    }

    /**
     * 프로젝트의 보드 조회
     * - 프로젝트에 연결된 보드 반환
     * - 보드가 없으면 예외 발생
     */
    public Board getBoardByProject(Long projectId) {

        // 프로젝트 확인
        if (!projectRepository.existsById(projectId)) {
            throw new GlobalException(GlobalErrorCode.NOT_FOUND_BOARD);
        }

        // 보드 조회
        return boardRepository.findByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_BOARD));
    }

    /**
     * 보드 제목 수정
     * - 보드 소유자인지 확인 (프로젝트 소유자 = 보드 소유자)
     */
    public Board updateBoardTitle(Long boardId, Long userId, String newTitle) {
        // TODO: 보드 존재 확인

        // TODO: 보드 소유권 확인 (프로젝트 소유자인지)

        // TODO: 제목 수정

        return null; // 임시 반환
    }

    /**
     * 보드 삭제
     * - 프로젝트 삭제 시 자동으로 삭제되므로 직접 삭제는 제한적
     * - 권한 확인 필요
     */
    public void deleteBoard(Long boardId, Long userId) {
        // TODO: 보드 존재 확인

        // TODO: 권한 확인

        // TODO: 보드 삭제 (연관된 컬럼, 카드도 cascade로 자동 삭제)
    }

    /**
     * 보드 존재 여부 확인
     */
    public boolean existsBoardByProject(Long projectId) {
        // TODO: 구현
        return false;
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
        // TODO: 구현
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