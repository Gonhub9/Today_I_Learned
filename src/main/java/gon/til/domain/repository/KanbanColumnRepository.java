package gon.til.domain.repository;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.KanbanColumn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {

    // 보드별 컬럼 순서대로
    List<KanbanColumn> findByBoardOrderByPosition(Board board);
    List<KanbanColumn> findByBoardIdOrderByPosition(Long boardId);
}
