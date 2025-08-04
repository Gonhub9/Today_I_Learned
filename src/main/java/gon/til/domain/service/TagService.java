package gon.til.domain.service;

import gon.til.domain.common.TagColor;
import gon.til.domain.dto.tag.TagCreateRequest;
import gon.til.domain.dto.tag.TagUpdateRequest;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.Tag;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.TagRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public Tag createTag(Long projectId, TagCreateRequest request) {

        Project project = getProjectById(projectId);

        validateProject(project.getUser().getId(), project.getTitle());

        if (!TagColor.isValidColor(request.getColor())) {
            throw new GlobalException(GlobalErrorCode.INVALID_COLOR_NAME);
        }

        TagColor tagColor = TagColor.from(request.getColor().toUpperCase());

        Tag tag = Tag.builder()
                .name(request.getName())
                .color(tagColor.getHexCode())
                .build();

        return tagRepository.save(tag);
    }

    // TODO : 프로젝트로 조회
    public List<Tag> getTagByProject(Long projectId) {
        List<Tag> tags = tagRepository.findByProjectId(projectId);

        if (tags.isEmpty()) {
            throw new GlobalException(GlobalErrorCode.NOT_FOUND_TAG);
        }

        return tags;
    }

    // TODO : 카드로 조회
    public List<Tag> getTagByCard(Long cardId) {
        List<Tag> tags = tagRepository.findByCardId(cardId);

        if (tags.isEmpty()) {
            throw new GlobalException(GlobalErrorCode.NOT_FOUND_TAG);
        }

        return tags;
    }

    // TODO : 태그 삭제
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = getTagById(tagId);

        tagRepository.delete(tag);
    }

    // TODO : 태그 수정
    @Transactional
    public Tag updateTag(Long tagId, TagUpdateRequest request) {
        Tag tag = getTagById(tagId);

        if (!TagColor.isValidColor(request.getColor())) {
            throw new GlobalException(GlobalErrorCode.INVALID_COLOR_NAME);
        }

        tag.updateTag(request.getName(), request.getColor());

        return tag;
    }

    // 헬퍼 메소드
    private Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_TAG));
    }

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_PROJECT));
    }



    // 프로젝트 검증
    private void validateProject(Long userId, String title) {

        // Project 이름 중복 검사
        if (projectRepository.existsByUserIdAndTitle(userId, title)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_PROJECT_TITLE);
        }
    }
}
