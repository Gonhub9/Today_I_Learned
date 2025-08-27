package gon.til.domain.controller;

import gon.til.domain.dto.tag.TagCreateRequest;
import gon.til.domain.dto.tag.TagResponse;
import gon.til.domain.dto.tag.TagUpdateRequest;
import gon.til.domain.entity.User;
import gon.til.domain.service.TagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tag", description = "태그 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<TagResponse> createTag(
        @PathVariable Long projectId,
        @AuthenticationPrincipal User user,
        @Valid @RequestBody TagCreateRequest request) {
        TagResponse response = tagService.createTag(projectId, user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<TagResponse>> getTagsByProject(
        @PathVariable Long projectId,
        @AuthenticationPrincipal User user) {
        List<TagResponse> responses = tagService.getTagsByProject(projectId, user.getId());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
        @PathVariable Long tagId,
        @AuthenticationPrincipal User user,
        @Valid @RequestBody TagUpdateRequest request) {
        TagResponse response = tagService.updateTag(tagId, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
        @PathVariable Long tagId,
        @AuthenticationPrincipal User user) {
        tagService.deleteTag(tagId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
