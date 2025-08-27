package gon.til.domain.repository;

import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUser(User user);

    @Query("SELECT p FROM Project p WHERE p.user.id = :userId")
    List<Project> findByUserId(Long userId);

    boolean existsByTitleAndUser(String title, User user);

    boolean existsByTitleAndUserIdAndIdNot(String title, Long userId, Long id);
}
