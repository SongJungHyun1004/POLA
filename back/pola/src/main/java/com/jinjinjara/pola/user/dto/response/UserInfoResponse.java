package com.jinjinjara.pola.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
