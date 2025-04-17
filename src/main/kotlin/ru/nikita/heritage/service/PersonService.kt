package ru.nikita.heritage.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import ru.nikita.heritage.api.Person
import ru.nikita.heritage.entity.PersonEntity
import ru.nikita.heritage.repository.PersonRepository

/**
 * Класс с логикой работы с людьми
 * @author sibmaks
 * @since 0.0.1
 */
@Service
class PersonService(
    val personRepository: PersonRepository
) {

    /**
     * Добавление человека в Базу данных
     */
    fun add(person: Person): Long {
        val entity = personRepository.save(
            PersonEntity(
                id = 0,
                lastName = person.lastName,
                firstName = person.firstName,
                middleName = person.middleName,
                birthDate = person.birthDate,
                deathDate = person.deathDate
            )
        )
        return entity.id
    }

    /**
     * Обновление человека в Базе данных
     */
    @Transactional
    fun update(id: Long, person: Person): Long {
        var entity = personRepository.findByIdLocked(id)
            .orElseThrow { IllegalArgumentException("Человек с id: $id не найден") }
        entity.apply {
            lastName = person.lastName
            firstName = person.firstName
            middleName = person.middleName
            birthDate = person.birthDate
            deathDate = person.deathDate
        }
        entity = personRepository.save(entity)
        return entity.id
    }

    /**
     * Получить человека из БД
     */
    fun getById(id: Long): Person {
        val entity = personRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Человек с id: $id не найден") }
        return Person(
            lastName = entity.lastName,
            firstName = entity.firstName,
            middleName = entity.middleName,
            birthDate = entity.birthDate,
            deathDate = entity.deathDate
        )
    }

    /**
     * Удалить человека в БД
     */
    fun deleteById(id: Long) {
        personRepository.deleteById(id)
    }

}