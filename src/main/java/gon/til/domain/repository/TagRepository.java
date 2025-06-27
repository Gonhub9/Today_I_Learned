package gon.til.domain.repository;

import gon.til.domain.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    List<Tag> findByNameContaining(String keyword);
    boolean existsByName(String name);
}
