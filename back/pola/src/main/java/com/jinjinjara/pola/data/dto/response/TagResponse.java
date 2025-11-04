package com.jinjinjara.pola.data.dto.response;


import com.jinjinjara.pola.data.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String tagName;

    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(tag.getId(), tag.getTagName());
    }
}
