package gon.til.domain.repository;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.KanbanColumn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {

    // 보드별 컬럼 순서대로
    List<KanbanColumn> findByBoardOrderByPosition(Board board);
    List<KanbanColumn> findByBoardIdOrderByPosition(Long boardId);

    // 특정 boardId 내에서 같은 name을 가진 컬럼이 있는지 확인
    boolean existsByBoardIdAndTitle(Long boardId, String title);

    boolean existsByBoardIdAndTitleAndIdNot(Long boardId, String title, Long id);

    // 전부 찾아냄
    List<KanbanColumn> findAllByIdIn(List<Long> columnId);
}
