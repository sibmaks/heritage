package ru.nikita.heritage.api

import java.time.LocalDate

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class Person(
    // фамилия
    val lastName: String,
    // имя
    val firstName: String,
    // отчество
    val middleName: String? = null,
    // дата рождения
    val birthDate: LocalDate? = null,
    // дата смерти
    val deathDate: LocalDate? = null,
)
