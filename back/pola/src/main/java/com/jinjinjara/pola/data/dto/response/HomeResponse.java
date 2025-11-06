package com.jinjinjara.pola.data.dto.response;

import lombok.*;
import java.util.List;


@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class HomeResponse {
    private List<CategorySection> categories;
    private List<DataResponse> favorites;
    private List<DataResponse> reminds;
    private List<DataResponse> timeline;
}