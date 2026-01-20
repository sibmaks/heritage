package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class PersonSearchResponse(
    val persons: List<PersonSearchItem>,
    val hasMore: Boolean,
)
