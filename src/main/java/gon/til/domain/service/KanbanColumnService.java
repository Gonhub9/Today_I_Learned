package gon.til.domain.service;

import gon.til.domain.dto.kanbancolumn.KanbanColumnCreateRequest;
import gon.til.domain.dto.kanbancolumn.KanbanColumnUpdateRequest;
import gon.til.domain.entity.Board;
import gon.til.domain.entity.KanbanColumn;
import gon.til.domain.repository.BoardRepository;
import gon.til.domain.repository.KanbanColumnRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 칸반 보드의 컬럼(열)과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 컬럼 생성, 조회, 수정, 삭제, 순서 변경 및 기본 컬럼 생성을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨에 트랜잭션을 적용하여 모든 public 메서드가 하나의 트랜잭션으로 실행되도록 합니다.
public class KanbanColumnService {

    private final KanbanColumnRepository kanbanColumnRepository;
    private final BoardRepository boardRepository;

    /**
     * 특정 보드에 새로운 컬럼을 생성합니다.
     * 컬럼의 위치(position)는 자동으로 가장 마지막에 추가됩니다.
     * 같은 보드 내에서 동일한 이름의 컬럼은 생성할 수 없습니다.
     *
     * @param boardId 컬럼을 추가할 보드의 ID
     * @param userId  요청을 보낸 사용자의 ID (권한 확인용)
     * @param request   새로 생성할 컬럼의 이름
     * @return 생성된 컬럼 엔티티
     */
    public KanbanColumn createColumn(Long boardId, Long userId, KanbanColumnCreateRequest request) {
        // 1. ID를 사용하여 보드 엔티티를 조회합니다. 보드가 없으면 예외를 발생시킵니다.
        Board board = getBoardById(boardId);

        // 2. 사용자가 해당 보드를 소유하고 있는지 권한을 확인하고, 컬럼명이 중복되지 않는지 검증합니다.
        validateBoardOwnership(board, userId);

        validateDuplicateColumnTitle(board.getId(), request.getTitle());

        // 5. 생성된 컬럼을 데이터베이스에 저장하고 반환합니다.
        return createAndSaveColumn(board, request.getTitle());
    }

    /**
     * 특정 보드에 속한 모든 컬럼을 위치(position) 순서대로 정렬하여 조회합니다.
     *
     * @param boardId 조회할 보드의 ID
     * @param userId  요청을 보낸 사용자의 ID (권한 확인용)
     * @return 위치 순으로 정렬된 컬럼 리스트
     */
    public List<KanbanColumn> getColumnsByBoard(Long boardId, Long userId) {
        // 1. ID를 사용하여 보드 엔티티를 조회하고, 사용자의 보드 소유 권한을 확인합니다.
        Board board = getBoardById(boardId);
        validateBoardOwnership(board, userId);

        // 2. 해당 보드에 속한 모든 컬럼을 position 순으로 정렬하여 데이터베이스에서 조회 후 반환합니다.
        return kanbanColumnRepository.findByBoardIdOrderByPosition(boardId);
    }

    /**
     * 기존 컬럼의 제목을 수정합니다.
     *
     * @param columnId 수정할 컬럼의 ID
     * @param userId   요청을 보낸 사용자의 ID (권한 확인용)
     * @param request 새로운 컬럼 제목
     * @return 제목이 수정된 컬럼 엔티티
     */
    public KanbanColumn updateColumnTitle(Long columnId, Long userId, KanbanColumnUpdateRequest request) {
        // 1. ID를 사용하여 컬럼 엔티티를 조회합니다.
        KanbanColumn column = getColumnById(columnId);

        // 2. 사용자가 해당 컬럼을 수정할 권한이 있는지 확인합니다.
        validateColumnOwnership(column, userId);

        // 3. 새로운 제목이 해당 보드 내에서 다른 컬럼과 중복되지 않는지 확인합니다. (자기 자신은 제외)
        validateDuplicateColumnTitle(column.getBoard().getId(), request.getTitle(), columnId);

        // 4. 컬럼의 제목을 새로운 제목으로 업데이트하고, 변경된 내용을 반환합니다.
        column.updateColumn(request.getTitle());
        return column;
    }

    /**
     * 컬럼의 순서를 변경합니다. (예: 드래그 앤 드롭 기능)
     * 요청받은 순서대로 모든 관련 컬럼의 위치(position) 값을 업데이트합니다.
     *
     * @param boardId   순서를 변경할 컬럼들이 속한 보드의 ID
     * @param userId    요청한 사용자의 ID (권한 확인용)
     * @param columnIds 새로운 순서대로 정렬된 컬럼 ID 목록
     * @return 위치가 업데이트된 컬럼 목록
     */
    public List<KanbanColumn> updateColumnPositions(Long boardId, Long userId, List<Long> columnIds) {
        // 1. 보드 존재 여부 및 사용자 권한을 확인합니다.
        Board board = getBoardById(boardId);
        validateBoardOwnership(board, userId);

        // 2. 요청된 ID 목록으로 모든 컬럼을 한 번에 조회합니다.
        List<KanbanColumn> columns = kanbanColumnRepository.findAllByIdIn(columnIds);

        // 2-1. 요청된 ID의 수와 실제 조회된 컬럼의 수가 다르면, 존재하지 않는 컬럼이 있다는 의미이므로 예외를 발생시킵니다.
        if (columns.size() != columnIds.size()) {
            throw new GlobalException(GlobalErrorCode.NOT_FOUND_COLUMN);
        }

        // 2-2. 조회된 모든 컬럼이 실제로 해당 보드에 속해 있는지 확인합니다. 다른 보드의 컬럼을 수정하려는 시도를 막습니다.
        for (KanbanColumn column : columns) {
            if (!column.getBoard().getId().equals(boardId)) {
                throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_COLUMN);
            }
        }

        // 3. 빠른 조회를 위해 컬럼 ID를 키로, 컬럼 객체를 값으로 하는 맵을 생성합니다.
        Map<Long, KanbanColumn> columnMap = columns.stream()
            .collect(Collectors.toMap(KanbanColumn::getId, column -> column));

        // 4. 새로운 순서(columnIds)에 따라 각 컬럼의 position 값을 1부터 순차적으로 업데이트합니다.
        for (int i = 0; i < columnIds.size(); i++) {
            Long currentColumnId = columnIds.get(i);
            KanbanColumn column = columnMap.get(currentColumnId);

            if (column != null) {
                column.updatePosition(i + 1); // position은 1부터 시작
            }
        }

        // 5. 변경된 컬럼 목록을 position 순으로 다시 정렬하여 반환합니다.
        columns.sort(Comparator.comparing(KanbanColumn::getPosition));
        return columns;
    }

    /**
     * 특정 컬럼을 삭제합니다.
     * JPA의 Cascade 설정에 따라 이 컬럼에 속한 모든 카드(Card)도 함께 삭제됩니다.
     * 삭제 후, 남아있는 컬럼들의 순서를 재정렬합니다.
     *
     * @param columnId 삭제할 컬럼의 ID
     * @param userId   요청을 보낸 사용자의 ID (권한 확인용)
     */
    public void deleteColumn(Long columnId, Long userId) {
        // 1. 삭제할 컬럼을 조회하고, 해당 컬럼이 속한 보드 정보를 가져옵니다.
        KanbanColumn column = getColumnById(columnId);
        Board board = column.getBoard();

        // 2. 사용자가 해당 컬럼을 삭제할 권한이 있는지 확인합니다.
        validateColumnOwnership(column, userId);

        // 3. 컬럼을 데이터베이스에서 삭제합니다. (연관된 카드들도 함께 삭제됨)
        kanbanColumnRepository.delete(column);

        // 4. 컬럼이 삭제되었으므로, 남아있는 컬럼들의 position 값을 순서대로 재정렬합니다.
        reorderPositions(board.getId());
    }

    /**
     * 새로운 보드가 생성될 때 호출되어 기본 컬럼들을 생성합니다.
     * ("할 일", "진행 중", "완료", "복습 필요")
     *
     * @param board 기본 컬럼을 생성할 보드 엔티티
     */
    @Transactional
    public void createDefaultColumns(Board board) {
        // createColumn 메서드를 재사용하여 4개의 기본 컬럼을 생성합니다.
        createAndSaveColumn(board, "할 일");
        createAndSaveColumn(board, "진행 중");
        createAndSaveColumn(board, "완료");
        createAndSaveColumn(board, "복습 필요");
    }

    // ===== private 헬퍼 메서드들 =====

    /**
     * ID로 보드를 조회하고, 없으면 예외를 발생시키는 헬퍼 메서드입니다.
     */
    private Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
            .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_BOARD));
    }

    /**
     * ID로 컬럼을 조회하고, 없으면 예외를 발생시키는 헬퍼 메서드입니다.
     */
    private KanbanColumn getColumnById(Long columnId) {
        return kanbanColumnRepository.findById(columnId)
            .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_COLUMN));
    }

    /**
     * 사용자가 특정 컬럼에 대한 소유권(수정/삭제 권한)이 있는지 확인합니다.
     * 컬럼 -> 보드 -> 프로젝트 -> 사용자 순으로 소유 관계를 확인합니다.
     */
    private void validateColumnOwnership(KanbanColumn column, Long userId) {
        Long boardOwnerId = column.getBoard().getProject().getUser().getId();
        if (!boardOwnerId.equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_COLUMN);
        }
    }

    /**
     * 사용자가 특정 보드에 대한 소유권(수정/삭제 권한)이 있는지 확인합니다.
     * 보드 -> 프로젝트 -> 사용자 순으로 소유 관계를 확인합니다.
     */
    private void validateBoardOwnership(Board board, Long userId) {
        Long projectOwnerId = board.getProject().getUser().getId();
        if (!projectOwnerId.equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_BOARD);
        }
    }

    /**
     * 새로운 컬럼의 제목이 해당 보드 내에서 중복되는지 확인합니다. (생성 시 사용)
     */
    private void validateDuplicateColumnTitle(Long boardId, String title) {
        if (kanbanColumnRepository.existsByBoardIdAndTitle(boardId, title)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_COLUMN);
        }
    }

    /**
     * 컬럼 제목 수정 시, 새로운 제목이 자기 자신을 제외한 다른 컬럼들과 중복되는지 확인합니다.
     */
    private void validateDuplicateColumnTitle(Long boardId, String title, Long excludeColumnId) {
        if (kanbanColumnRepository.existsByBoardIdAndTitleAndIdNot(boardId, title, excludeColumnId)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_COLUMN_TITLE);
        }
    }

    /**
     * 새로 생성될 컬럼의 다음 위치(position) 값을 계산합니다.
     * 현재 보드의 컬럼 수에 1을 더한 값을 반환합니다.
     */
    private int getNextPosition(Long boardId) {
        // position이 1부터 순차적으로 증가하므로, 현재 컬럼의 개수가 다음 position의 값이 됩니다.
        return kanbanColumnRepository.findByBoardIdOrderByPosition(boardId).size() + 1;
    }

    /**
     * 컬럼 삭제 후, 남은 컬럼들의 위치(position)를 1부터 순서대로 재정렬합니다.
     */
    private void reorderPositions(Long boardId) {
        // 1. 해당 보드의 모든 컬럼을 position 순으로 조회합니다.
        List<KanbanColumn> columns = kanbanColumnRepository.findByBoardIdOrderByPosition(boardId);

        // 2. 루프를 돌면서 각 컬럼의 position을 1부터 순서대로 다시 할당합니다.
        for (int i = 0; i < columns.size(); i++) {
            KanbanColumn column = columns.get(i);
            column.updatePosition(i + 1); // position을 1, 2, 3, ... 순으로 업데이트
        }
    }

    private KanbanColumn createAndSaveColumn(Board board, String title) {

        int newPosition = getNextPosition(board.getId());

        KanbanColumn column = KanbanColumn.builder()
                .board(board)
                .title(title)
                .position(newPosition)
                .build();

        return kanbanColumnRepository.save(column);
    }
}
