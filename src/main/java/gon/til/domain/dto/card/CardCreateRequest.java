package gon.til.domain.dto.card;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardCreateRequest {
    @NotBlank(message = "제목은 필수 입력 사항입니다.")
    private String title;

    private String content;

    @NotBlank(message = "번호가 누락되었습니다.")
    private Integer position;

    private List<Long> tagIds;
}
