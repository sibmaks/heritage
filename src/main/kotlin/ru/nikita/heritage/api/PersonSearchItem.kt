package ru.nikita.heritage.api

import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Schema(description = "Person summary returned by the search API.")
data class PersonSearchItem(
    @field:Schema(description = "Person identifier.")
    val id: Long,
    @field:Schema(
        description = "Full name including married last name when present (e.g. 'Ivanova Maria (Petrova)')."
    )
    val fullName: String,
    @field:Schema(description = "Gender flag: true for male, false for female.")
    val gender: Boolean,
    @field:Schema(description = "Birth date.")
    val birthDate: FlexibleDate? = null,
    @field:Schema(description = "Death date.")
    val deathDate: FlexibleDate? = null,
)
