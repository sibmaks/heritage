package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class PersonMarriage(
    val id: Long,
    val husbandId: Long,
    val wifeId: Long,
    val status: MarriageStatus,
    val registrationDate: FlexibleDate? = null,
    val registrationPlace: String? = null,
    val divorceDate: FlexibleDate? = null,
)
