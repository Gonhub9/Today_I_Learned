package gon.til.domain.dto.kanbancolumn;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KanbanColumnUpdateRequest {
    @NotNull(message = "보드 ID가 누락되었습니다.")
    private Long boardId;
    @NotBlank(message = "제목은 필수 입력 사항입니다.")
    private String title;
}
