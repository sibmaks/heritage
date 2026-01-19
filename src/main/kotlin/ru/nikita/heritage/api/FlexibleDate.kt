package ru.nikita.heritage.api

import java.time.LocalDate

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class FlexibleDate(
    val type: FlexibleDateType,
    val date: LocalDate? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
