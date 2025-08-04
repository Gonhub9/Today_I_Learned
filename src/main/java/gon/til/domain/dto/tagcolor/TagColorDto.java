package gon.til.domain.dto.tagcolor;

import gon.til.domain.common.TagColor;
import lombok.Getter;

@Getter
public class TagColorDto {
    private final String name;
    private final String hexCode;

    public TagColorDto(TagColor color) {
        this.name = color.name();
        this.hexCode = color.getHexCode();
    }
}
