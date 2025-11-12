package com.jinjinjara.pola.domain.model

data class RagSearchResult(
    val answer: String,
    val sources: List<Source>
)

data class Source(
    val id: Long,
    val tags: List<String>,
    val src: String,
    val context: String,
    val relevanceScore: Double
)
