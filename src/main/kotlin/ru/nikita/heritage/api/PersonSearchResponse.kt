package ru.nikita.heritage.api

import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Schema(description = "Response payload for person search.")
data class PersonSearchResponse(
    @field:Schema(description = "List of matching persons.")
    val persons: List<PersonSearchItem>,
    @field:Schema(description = "True when there are more matches than the configured limit.")
    val hasMore: Boolean,
)
