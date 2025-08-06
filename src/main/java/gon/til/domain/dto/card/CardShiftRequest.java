package gon.til.domain.dto.card;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardShiftRequest {

    @NotNull(message = "새로운 컬럼 ID는 필수입니다.")
    private Long newColumnId;

    @NotNull(message = "새로운 위치 정보는 필수입니다.")
    private Integer newPosition;
}
