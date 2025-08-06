package gon.til.domain.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequest {

    @NotNull(message = "프로젝트 ID는 필수 입력 사항입니다.")
    private Long projectId;

    @NotBlank(message = "보드 제목은 필수 입력 사항입니다.")
    private String title;
}
