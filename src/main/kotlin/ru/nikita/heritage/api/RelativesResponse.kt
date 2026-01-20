package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class RelativesResponse(
    val persons: List<RelativePerson>,
    val marriages: List<PersonMarriage>,
)
