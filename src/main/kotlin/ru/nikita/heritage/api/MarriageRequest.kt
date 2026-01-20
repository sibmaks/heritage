package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class MarriageRequest(
    val spouseId: Long,
    val registrationDate: FlexibleDate? = null,
    val registrationPlace: String? = null,
    val divorceDate: FlexibleDate? = null,
)
