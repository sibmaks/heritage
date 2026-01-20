package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class RelativePerson(
    val id: Long,
    val lastName: String? = null,
    val firstName: String? = null,
    val gender: Boolean,
    val marriedLastName: String? = null,
    val birthDate: FlexibleDate? = null,
    val deathDate: FlexibleDate? = null,
    val motherId: Long? = null,
    val fatherId: Long? = null,
)
