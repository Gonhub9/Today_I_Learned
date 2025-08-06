package gon.til.domain.dto.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectCreateRequest {
    @NotBlank(message = "제목은 필수 입력 사항입니다.")
    private String title;
    @NotBlank(message = "프로젝트 설명은 필수 입력 사항입니다.")
    private String description;
    @NotBlank(message = "카테고리는 필수 입력 사항입니다.")
    private String category;
}
