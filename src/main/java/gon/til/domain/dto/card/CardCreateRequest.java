package gon.til.domain.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {

    @NotNull(message = "컬럼 ID는 필수 입력 사항입니다.")
    private Long columnId;

    @NotBlank(message = "카드 제목은 필수 입력 사항입니다.")
    private String title;

    private String content;
}
