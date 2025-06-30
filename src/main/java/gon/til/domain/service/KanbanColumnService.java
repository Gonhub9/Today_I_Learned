package gon.til.domain.service;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.KanbanColumn;
import gon.til.domain.repository.BoardRepository;
import gon.til.domain.repository.KanbanColumnRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KanbanColumnService {

    private final KanbanColumnRepository kanbanColumnRepository;
    private final BoardRepository boardRepository;

    /**
     * 컬럼 생성
     * - 보드에 새로운 컬럼 추가
     * - position 자동 할당 (맨 뒤에 추가)
     * - 같은 보드 내 컬럼명 중복 불가
     */
    public KanbanColumn createColumn(Long boardId, Long userId, String title) {
        // TODO: 보드 존재 확인 및 권한 확인

        // TODO: 컬럼명 중복 확인

        // TODO: 다음 position 계산 (맨 뒤에 추가)

        // TODO: 컬럼 생성 및 저장

        return null; // 임시 반환
    }

    /**
     * 보드별 컬럼 목록 조회 (position 순서대로)
     * - 보드에 속한 모든 컬럼을 position 순으로 반환
     */
    public List<KanbanColumn> getColumnsByBoard(Long boardId, Long userId) {
        // TODO: 보드 존재 확인 및 권한 확인

        // TODO: 컬럼 목록 조회 (position 순)

        return null; // 임시 반환
    }

    /**
     * 컬럼 제목 수정
     * - 컬럼 소유권 확인 (보드 소유자 = 컬럼 관리자)
     * - 같은 보드 내 중복 제목 확인
     */
    public KanbanColumn updateColumnTitle(Long columnId, Long userId, String newTitle) {
        // TODO: 컬럼 존재 확인

        // TODO: 권한 확인 (보드 소유자인지)

        // TODO: 중복 제목 확인

        // TODO: 제목 수정

        return null; // 임시 반환
    }

    /**
     * 컬럼 순서 변경
     * - 드래그 앤 드롭으로 컬럼 순서 변경
     * - position 값들을 재정렬
     */
    public List<KanbanColumn> updateColumnPositions(Long boardId, Long userId, List<Long> columnIds) {
        // TODO: 보드 존재 확인 및 권한 확인

        // TODO: 컬럼들이 모두 해당 보드에 속하는지 확인

        // TODO: position 값들을 새로 할당

        // TODO: 업데이트된 컬럼 목록 반환

        return null; // 임시 반환
    }

    /**
     * 컬럼 삭제
     * - 컬럼과 연관된 모든 카드도 함께 삭제 (cascade)
     * - 삭제 후 나머지 컬럼들의 position 재정렬
     */
    public void deleteColumn(Long columnId, Long userId) {
        // TODO: 컬럼 존재 확인

        // TODO: 권한 확인

        // TODO: 컬럼 삭제 (카드들도 cascade로 삭제)

        // TODO: 남은 컬럼들의 position 재정렬
    }

    /**
     * 기본 컬럼들 생성 (보드 생성 시 호출)
     * - "할 일", "진행중", "완료", "복습필요" 기본 컬럼 생성
     */
    public void createDefaultColumns(Long boardId) {
        // TODO: 기본 컬럼 4개 생성
        // 1. 할 일 (position: 1)
        // 2. 진행중 (position: 2)
        // 3. 완료 (position: 3)
        // 4. 복습필요 (position: 4)
    }

    // ===== private 헬퍼 메서드들 =====

    /**
     * 보드 존재 확인 및 조회
     */
    private Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_BOARD));
    }

    /**
     * 컬럼 존재 확인 및 조회
     */
    private KanbanColumn getColumnById(Long columnId) {
        return kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_BOARD));
    }

    /**
     * 컬럼 소유권 확인
     * - 컬럼 → 보드 → 프로젝트 → 사용자 경로로 권한 확인
     */
    private void validateColumnOwnership(KanbanColumn column, Long userId) {
        // TODO: 구현
    }

    /**
     * 보드 권한 확인
     * - 보드 → 프로젝트 → 사용자 경로로 권한 확인
     */
    private void validateBoardOwnership(Board board, Long userId) {
        // TODO: 구현
    }

    /**
     * 컬럼명 중복 확인
     * - 같은 보드 내에서 컬럼명 중복 불가
     */
    private void validateDuplicateColumnTitle(Long boardId, String title) {
        // TODO: 구현
    }

    /**
     * 컬럼명 중복 확인 (수정 시)
     * - 자기 자신 제외하고 중복 확인
     */
    private void validateDuplicateColumnTitle(Long boardId, String title, Long excludeColumnId) {
        // TODO: 구현
    }

    /**
     * 다음 position 값 계산
     * - 해당 보드의 마지막 position + 1
     */
    private int getNextPosition(Long boardId) {
        // TODO: 구현
        return 0;
    }

    /**
     * 컬럼들의 position 재정렬
     * - 삭제 후 빈 자리 없이 1, 2, 3... 순으로 재정렬
     */
    private void reorderPositions(Long boardId) {
        // TODO: 구현
    }
}