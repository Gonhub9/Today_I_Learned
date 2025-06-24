package gon.til.repository;

import gon.til.entity.Card;
import gon.til.entity.KanbanColumn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    // 컬럼별 카드들을 순서대로
    List<Card> findByKanbanColumnOrderByPosition(KanbanColumn column);
    List<Card> findByKanbanColumnIdOrderByPosition(Long columnId);

    // 검색
    List<Card> findByTitleContaining(String keyword);
    List<Card> findByContentContaining(String keyword);
}
