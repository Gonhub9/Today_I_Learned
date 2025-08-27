package gon.til.domain.repository;

import gon.til.domain.entity.Board;
import gon.til.domain.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // Project와 1:1 관계
    Optional<Board> findByProject(Project project);
    Optional<Board> findByProjectIdAndId(Long projectId, Long boardId);

    boolean existsByProjectId(Long projectId);
    boolean existsById(Long boardId);

    @Query("SELECT DISTINCT b FROM Board b LEFT JOIN FETCH b.columns")
    List<Board> findAllWithColumns();

    @Query("SELECT b FROM Board b WHERE b.project.user.id = :userId")
    List<Board> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) > 0 FROM Board b WHERE b.project.user.id = :userId AND b.title = :title AND b.id <> :boardId")
    boolean existsByTitleAndUserIdAndIdNot(String title, Long userId, Long boardId);
}
