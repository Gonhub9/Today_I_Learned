package gon.til.ServiceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Collections;
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
@DisplayName("TagService 통합 테스트")
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
        user = User.builder().id(1L).build();
        project = Project.builder().id(1L).user(user).title("Test Project").build();
        tag = Tag.builder().id(1L).name("Test Tag").color(TagColor.PASTEL_RED.getHexCode()).build();
    }

    @Nested
    @DisplayName("태그 생성 테스트")
    class CreateTag {

        @Test
        @DisplayName("성공")
        void createTag_success() {
            // given
            TagCreateRequest request = new TagCreateRequest("New Tag", "GREEN");
            when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
            when(projectRepository.existsByUserIdAndTitle(anyLong(), anyString())).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Tag createdTag = tagService.createTag(project.getId(), request);

            // then
            assertNotNull(createdTag);
            assertEquals(request.getName(), createdTag.getName());
            assertEquals(TagColor.PASTEL_GREEN.getHexCode(), createdTag.getColor());
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 색상")
        void createTag_fail_invalidColor() {
            // given
            TagCreateRequest request = new TagCreateRequest("New Tag", "INVALID_COLOR");
            when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
            when(projectRepository.existsByUserIdAndTitle(anyLong(), anyString())).thenReturn(false);

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.createTag(project.getId(), request));
            assertEquals(GlobalErrorCode.INVALID_COLOR_NAME.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("태그 조회 테스트")
    class ReadTag {

        @Test
        @DisplayName("프로젝트 ID로 조회 성공")
        void getTagByProject_success() {
            // given
            when(tagRepository.findByProjectId(anyLong())).thenReturn(List.of(tag));

            // when
            List<Tag> tags = tagService.getTagByProject(project.getId());

            // then
            assertEquals(1, tags.size());
            assertEquals(tag.getName(), tags.getFirst().getName());
        }

        @Test
        @DisplayName("프로젝트 ID로 조회 실패 - 결과 없음")
        void getTagByProject_fail_notFound() {
            // given
            when(tagRepository.findByProjectId(anyLong())).thenReturn(Collections.emptyList());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.getTagByProject(project.getId()));
            assertEquals(GlobalErrorCode.NOT_FOUND_TAG.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("카드 ID로 조회 성공")
        void getTagByCard_success() {
            // given
            Long cardId = 1L;
            when(tagRepository.findByCardId(anyLong())).thenReturn(List.of(tag));

            // when
            List<Tag> tags = tagService.getTagByCard(cardId);

            // then
            assertEquals(1, tags.size());
        }

        @Test
        @DisplayName("카드 ID로 조회 실패 - 결과 없음")
        void getTagByCard_fail_notFound() {
            // given
            Long cardId = 1L;
            when(tagRepository.findByCardId(anyLong())).thenReturn(Collections.emptyList());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.getTagByCard(cardId));
            assertEquals(GlobalErrorCode.NOT_FOUND_TAG.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("태그 수정 테스트")
    class UpdateTag {

        @Test
        @DisplayName("성공")
        void updateTag_success() {
            // given
            TagUpdateRequest request = new TagUpdateRequest("Updated Tag", "BLUE");
            when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));

            // when
            Tag updatedTag = tagService.updateTag(tag.getId(), request);

            // then
            assertEquals(request.getName(), updatedTag.getName());
            assertEquals(TagColor.PASTEL_BLUE.getHexCode(), updatedTag.getColor());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 태그")
        void updateTag_fail_tagNotFound() {
            // given
            TagUpdateRequest request = new TagUpdateRequest("Updated Tag", "BLUE");
            when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.updateTag(999L, request));
            assertEquals(GlobalErrorCode.NOT_FOUND_TAG.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("태그 삭제 테스트")
    class DeleteTag {

        @Test
        @DisplayName("성공")
        void deleteTag_success() {
            // given
            when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));
            doNothing().when(tagRepository).delete(any(Tag.class));

            // when
            tagService.deleteTag(tag.getId());

            // then
            verify(tagRepository).findById(tag.getId());
            verify(tagRepository).delete(tag);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 태그")
        void deleteTag_fail_notFound() {
            // given
            when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                () -> tagService.deleteTag(999L));
            assertEquals(GlobalErrorCode.NOT_FOUND_TAG.getCode(), exception.getErrorCode());
        }
    }
}