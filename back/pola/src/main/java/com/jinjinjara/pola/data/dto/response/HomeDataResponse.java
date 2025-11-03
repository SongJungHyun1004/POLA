package com.jinjinjara.pola.data.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jinjinjara.pola.user.dto.response.UserInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataResponse {

    @JsonProperty("user_info")
    private UserInfoResponse userInfo;

    @JsonProperty("favorite")
    private List<DataResponse> favoriteData;

    @JsonProperty("remind")
    private List<DataResponse> remindData;

    @JsonProperty("recent")
    private List<DataResponse> recentData;

    @JsonProperty("category")
    private List<CategoryDataResponse> categoryData;
}
