package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class Person(
    val id: Long? = null,
    // фамилия
    val lastName: String,
    // имя
    val firstName: String,
    // отчество
    val middleName: String? = null,
    // фамилия после замужества
    val marriedLastName: String? = null,
    // место рождения
    val birthPlace: String? = null,
    // место смерти
    val deathPlace: String? = null,
    // гибкая дата рождения
    val birthDate: FlexibleDate? = null,
    // гибкая дата смерти
    val deathDate: FlexibleDate? = null,
)
