package gon.til.domain.dto.cardtag;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardTagRequest {

    @NotNull(message = "태그 ID는 필수 입력 사항입니다.")
    private Long tagId;
}
