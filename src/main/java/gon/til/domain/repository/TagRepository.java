package gon.til.domain.repository;

import gon.til.domain.entity.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // Project ID로 모든 태그 조회
    List<Tag> findByProjectId(Long projectId);

    // Project ID와 태그 이름으로 존재 여부 확인 (생성 시 중복 검사)
    boolean existsByProjectIdAndName(Long projectId, String name);

    // Project ID, 태그 이름, 제외할 태그 ID로 존재 여부 확인 (수정 시 중복 검사)
    boolean existsByProjectIdAndNameAndIdNot(Long projectId, String name, Long id);
}
