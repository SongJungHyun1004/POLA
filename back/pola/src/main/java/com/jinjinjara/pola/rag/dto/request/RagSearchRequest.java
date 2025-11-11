package com.jinjinjara.pola.rag.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RagSearchRequest(
        @NotBlank String query
) {}