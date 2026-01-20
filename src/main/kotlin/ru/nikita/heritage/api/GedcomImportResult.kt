package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class GedcomImportResult(
    val persons: Int,
    val relationships: Int,
    val families: Int,
)
