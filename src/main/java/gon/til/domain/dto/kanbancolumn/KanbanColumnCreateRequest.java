package gon.til.domain.dto.kanbancolumn;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KanbanColumnCreateRequest {

    @NotBlank(message = "컬럼 제목은 필수 입력 사항입니다.")
    private String title;

    @NotNull(message = "보드 ID는 필수 입력 사항입니다.")
    private Long boardId;
}