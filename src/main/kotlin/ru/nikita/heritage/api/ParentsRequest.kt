package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class ParentsRequest(
    val motherId: Long? = null,
    val fatherId: Long? = null,
)
