package gon.til.domain.repository;

import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
    List<Project> findByUserAndCategory(User user, String category);
    Optional<Project> findByUserAndTitle(User user, String title);
    boolean existsByUserAndTitle(Long userId, String title);
}
