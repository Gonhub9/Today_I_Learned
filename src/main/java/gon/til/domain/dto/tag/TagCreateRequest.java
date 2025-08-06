package gon.til.domain.dto.tag;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagCreateRequest {
    private String name;
    private String color;

    public TagCreateRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
