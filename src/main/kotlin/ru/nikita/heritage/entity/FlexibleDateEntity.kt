package ru.nikita.heritage.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import ru.nikita.heritage.api.FlexibleDateType
import java.time.LocalDate

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Embeddable
data class FlexibleDateEntity(
    @Enumerated(EnumType.STRING)
    @Column(name = "date_type")
    var type: FlexibleDateType? = null,
    @Column(name = "date_value")
    var date: LocalDate? = null,
    @Column(name = "date_start")
    var startDate: LocalDate? = null,
    @Column(name = "date_end")
    var endDate: LocalDate? = null,
)
