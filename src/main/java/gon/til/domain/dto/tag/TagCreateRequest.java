package gon.til.domain.dto.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagCreateRequest {

    @NotBlank(message = "태그 이름은 필수 입력 사항입니다.")
    private String name;

    @NotBlank(message = "색상 코드는 필수 입력 사항입니다.")
    private String color;

    public TagCreateRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
