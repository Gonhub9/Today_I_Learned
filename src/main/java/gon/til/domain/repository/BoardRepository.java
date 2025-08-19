package gon.til.domain.repository;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // Project와 1:1 관계
    Optional<Board> findByProject(Project project);
    Optional<Board> findByProjectIdAndId(Long projectId, Long boardId);

    boolean existsByProjectId(Long projectId);
    boolean existsById(Long boardId);

    @Query("SELECT DISTINCT b FROM Board b LEFT JOIN FETCH b.columns")
    List<Board> findAllWithColumns();
}
