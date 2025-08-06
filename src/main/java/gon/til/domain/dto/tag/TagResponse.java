package gon.til.domain.dto.tag;

import gon.til.domain.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagResponse {

    private final Long id;
    private final String name;
    private final String color;

    public static TagResponse from(Tag tag) {
        return new TagResponse(
            tag.getId(),
            tag.getName(),
            tag.getColor()
        );
    }
}
