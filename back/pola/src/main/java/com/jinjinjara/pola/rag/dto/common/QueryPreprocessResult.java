package com.jinjinjara.pola.rag.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class QueryPreprocessResult {
    private final String cleanedQuery;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final QueryType queryType;
}
