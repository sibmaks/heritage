package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class PersonSearchItem(
    val id: Long,
    val fullName: String,
    val gender: Boolean,
    val birthDate: FlexibleDate? = null,
    val deathDate: FlexibleDate? = null,
)
