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
    public Tag createTag(Long projectId, Long userId, TagCreateRequest request) {
        Project project = getProjectById(projectId);
        validateProjectOwnership(project, userId);
        validateDuplicateTagName(projectId, request.getName());

        if (!TagColor.isValidColor(request.getColor())) {
            throw new GlobalException(GlobalErrorCode.INVALID_COLOR_NAME);
        }

        TagColor tagColor = TagColor.from(request.getColor().toUpperCase());

        Tag tag = Tag.builder()
                .project(project) // 프로젝트 연관관계 설정
                .name(request.getName())
                .color(tagColor.getHexCode())
                .build();

        return tagRepository.save(tag);
    }

    public List<Tag> getTagsByProject(Long projectId, Long userId) {
        Project project = getProjectById(projectId);
        validateProjectOwnership(project, userId);
        return tagRepository.findByProjectId(projectId);
    }

    @Transactional
    public void deleteTag(Long tagId, Long userId) {
        Tag tag = getTagById(tagId);
        validateTagOwnership(tag, userId);
        tagRepository.delete(tag);
    }

    @Transactional
    public Tag updateTag(Long tagId, Long userId, TagUpdateRequest request) {
        Tag tag = getTagById(tagId);
        validateTagOwnership(tag, userId);
        validateDuplicateTagName(tag.getProject().getId(), request.getName(), tagId);

        if (!TagColor.isValidColor(request.getColor())) {
            throw new GlobalException(GlobalErrorCode.INVALID_COLOR_NAME);
        }

        tag.updateTag(request.getName(), request.getColor());
        return tag;
    }

    // ===== private 헬퍼 메서드들 =====

    private Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_TAG));
    }

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_PROJECT));
    }

    private void validateProjectOwnership(Project project, Long userId) {
        if (!project.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_PROJECT);
        }
    }

    private void validateTagOwnership(Tag tag, Long userId) {
        if (!tag.getProject().getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.ACCESS_DENIED_TAG);
        }
    }

    private void validateDuplicateTagName(Long projectId, String name) {
        if (tagRepository.existsByProjectIdAndName(projectId, name)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_TAG_NAME);
        }
    }

    private void validateDuplicateTagName(Long projectId, String name, Long excludeTagId) {
        if (tagRepository.existsByProjectIdAndNameAndIdNot(projectId, name, excludeTagId)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_TAG_NAME);
        }
    }
}
