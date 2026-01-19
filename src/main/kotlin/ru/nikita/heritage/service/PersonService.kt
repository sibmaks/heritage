package ru.nikita.heritage.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikita.heritage.api.Person
import ru.nikita.heritage.converter.PersonConverter
import ru.nikita.heritage.repository.PersonRepository

/**
 * Класс с логикой работы с людьми
 * @author sibmaks
 * @since 0.0.1
 */
@Service
class PersonService(
    val personRepository: PersonRepository,
    val personConverter: PersonConverter
) {

    /**
     * Добавление человека в Базу данных
     */
    fun add(person: Person): Long {
        var entity = personConverter.map(person)
        entity = personRepository.save(entity)
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
            marriedLastName = person.marriedLastName
            birthPlace = person.birthPlace
            deathPlace = person.deathPlace
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
        return personConverter.map(entity)
    }

    /**
     * Удалить человека в БД
     */
    fun deleteById(id: Long) {
        personRepository.deleteById(id)
    }

}
