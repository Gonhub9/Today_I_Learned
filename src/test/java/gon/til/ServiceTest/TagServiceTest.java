package gon.til.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import gon.til.domain.common.TagColor;
import gon.til.domain.dto.tag.TagCreateRequest;
import gon.til.domain.dto.tag.TagUpdateRequest;
import gon.til.domain.entity.Project;
import gon.til.domain.entity.Tag;
import gon.til.domain.entity.User;
import gon.til.domain.repository.ProjectRepository;
import gon.til.domain.repository.TagRepository;
import gon.til.domain.service.TagService;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService 테스트")
class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectRepository projectRepository;

    private User user;
    private Project project;
    private Tag tag;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).displayName("testuser").build();
        project = Project.builder().id(1L).user(user).title("Test Project").build();
        tag = Tag.builder().id(1L).project(project).name("Test Tag").color(TagColor.PASTEL_RED.getHexCode()).build();
    }

    @Nested
    @DisplayName("태그 생성")
    class CreateTag {

        @Test
        @DisplayName("성공")
        void createTag_success() {
            // given
            TagCreateRequest request = new TagCreateRequest("New Tag", "GREEN");
            given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
            given(tagRepository.existsByProjectIdAndName(project.getId(), request.getName())).willReturn(false);
            given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Tag createdTag = tagService.createTag(project.getId(), user.getId(), request);

            // then
            assertNotNull(createdTag);
            assertEquals(request.getName(), createdTag.getName());
            assertEquals(TagColor.PASTEL_GREEN.getHexCode(), createdTag.getColor());
            assertEquals(project, createdTag.getProject());
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("실패 - 중복된 태그 이름")
        void createTag_fail_duplicateName() {
            // given
            TagCreateRequest request = new TagCreateRequest("Existing Tag", "BLUE");
            given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
            given(tagRepository.existsByProjectIdAndName(project.getId(), request.getName())).willReturn(true);

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.createTag(project.getId(), user.getId(), request));
            assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.DUPLICATE_TAG_NAME.getCode());
        }
    }

    @Nested
    @DisplayName("태그 수정")
    class UpdateTag {

        @Test
        @DisplayName("성공")
        void updateTag_success() {
            // given
            TagUpdateRequest request = new TagUpdateRequest("Updated Tag", "BLUE");
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));
            given(tagRepository.existsByProjectIdAndNameAndIdNot(anyLong(), anyString(), anyLong())).willReturn(false);

            // when
            Tag updatedTag = tagService.updateTag(tag.getId(), user.getId(), request);

            // then
            assertEquals(request.getName(), updatedTag.getName());
            assertEquals(TagColor.PASTEL_BLUE.getHexCode(), updatedTag.getColor());
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 태그")
        void updateTag_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
            TagUpdateRequest request = new TagUpdateRequest("Updated Tag", "BLUE");
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.updateTag(tag.getId(), otherUserId, request));
            assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_TAG.getCode());
        }
    }

    @Nested
    @DisplayName("태그 삭제")
    class DeleteTag {

        @Test
        @DisplayName("성공")
        void deleteTag_success() {
            // given
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));

            // when & then
            assertDoesNotThrow(() -> tagService.deleteTag(tag.getId(), user.getId()));
            verify(tagRepository).delete(tag);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 태그")
        void deleteTag_fail_accessDenied() {
            // given
            Long otherUserId = 99L;
            given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.deleteTag(tag.getId(), otherUserId));
            assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED_TAG.getCode());
        }
    }
}
