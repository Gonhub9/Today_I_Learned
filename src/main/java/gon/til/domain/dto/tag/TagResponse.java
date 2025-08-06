package gon.til.domain.dto.tag;

import gon.til.domain.entity.Tag;
import lombok.Getter;

@Getter
public class TagResponse {
    private final Long id;
    private final String name;
    private final String color;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
        this.color = tag.getColor();
    }
}
