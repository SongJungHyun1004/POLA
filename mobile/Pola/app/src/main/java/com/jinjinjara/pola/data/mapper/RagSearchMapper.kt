package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.RagSearchData
import com.jinjinjara.pola.data.remote.dto.response.SourceDto
import com.jinjinjara.pola.domain.model.RagSearchResult
import com.jinjinjara.pola.domain.model.Source

fun RagSearchData.toDomain(): RagSearchResult {
    return RagSearchResult(
        answer = answer,
        sources = sources.map { it.toDomain() }
    )
}

fun SourceDto.toDomain(): Source {
    return Source(
        id = id,
        tags = tags,
        src = src,
        context = context,
        relevanceScore = relevanceScore
    )
}
