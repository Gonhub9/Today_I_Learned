package gon.til.domain.dto.card;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardUpdateRequest {
    @NotBlank(message = "제목은 필수 입력 사항입니다.")
    private String title;

    private String content;
}
