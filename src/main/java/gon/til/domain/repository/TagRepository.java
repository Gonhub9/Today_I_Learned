package gon.til.domain.repository;

import gon.til.domain.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    List<Tag> findByNameContaining(String keyword);
    boolean existsByName(String name);

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.cardTags ct JOIN ct.card c WHERE c.project.id = :projectId")
    List<Tag> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.cardTags ct WHERE ct.card.id = :cardId")
    List<Tag> findByCardId(@Param("cardId") Long cardId);
}
