package gon.til.repository;

import gon.til.entity.Board;
import gon.til.entity.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // Project와 1:1 관계
    Optional<Board> findByProject(Project project);
    Optional<Board> findByProjectId(Long projectId);
}
