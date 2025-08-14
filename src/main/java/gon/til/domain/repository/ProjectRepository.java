package gon.til.domain.repository;

import gon.til.domain.entity.Project;
import gon.til.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUser(User user);

    boolean existsByTitleAndUser(String title, User user);
}
