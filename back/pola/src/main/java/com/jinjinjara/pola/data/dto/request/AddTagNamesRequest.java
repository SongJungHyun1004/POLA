package com.jinjinjara.pola.data.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddTagNamesRequest {
    private List<String> tagNames;
}
