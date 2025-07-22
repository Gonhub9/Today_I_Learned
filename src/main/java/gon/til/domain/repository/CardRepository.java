package gon.til.domain.repository;

import gon.til.domain.entity.Card;
import gon.til.domain.entity.KanbanColumn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long> {

    // 컬럼별 카드들을 순서대로
    List<Card> findByKanbanColumnOrderByPosition(KanbanColumn column);
    List<Card> findByKanbanColumnIdOrderByPosition(Long columnId);

    // 검색
    List<Card> findByTitleContaining(String keyword);
    List<Card> findByContentContaining(String keyword);

    // 특정 컬럼에서, 특정 'position' 보다 큰 카드들의 'position' 을 1씩 감소시키는 메소드 (카드가 빠져나갈 때)
    @Modifying
    @Query("UPDATE Card c SET c.position = c.position - 1 WHERE c.kanbanColumn.id = :columnId = :columnId AND c.position > :position")
    void decrementPositionsAfter(@Param("columnId") Long columnId, @Param("position") Integer position);

    // 특정 컬럼에서, 특정 'position' 보다 크거나 같은 카드들의 'position' 을 1씩 증가시키는 메소드 (카드가 새로 들어올 때)
    @Modifying
    @Query("UPDATE Card c SET c.position = c.position + 1 WHERE c.kanbanColumn.id = :columnId = :columnId AND c.position >= :position")
    void incrementPositionsFrom(@Param("columnId") Long columnId, @Param("position") Integer position);
}
