package gon.til.domain.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardCreateRequest {
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;
    @NotNull(message = "프로젝트 Id는 필수 항목입니다.")
    private Long projectId;
}
