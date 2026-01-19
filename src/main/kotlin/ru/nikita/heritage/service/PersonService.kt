package ru.nikita.heritage.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikita.heritage.api.MarriageStatus
import ru.nikita.heritage.api.Person
import ru.nikita.heritage.api.PersonMarriage
import ru.nikita.heritage.converter.PersonConverter
import ru.nikita.heritage.entity.DeathEntity
import ru.nikita.heritage.repository.MarriageRepository
import ru.nikita.heritage.repository.PersonRepository

/**
 * Класс с логикой работы с людьми
 * @author sibmaks
 * @since 0.0.1
 */
@Service
class PersonService(
    val personRepository: PersonRepository,
    val marriageRepository: MarriageRepository,
    val personConverter: PersonConverter
) {

    /**
     * Добавление человека в Базу данных
     */
    fun add(person: Person): Long {
        var entity = personConverter.map(person)
        entity.death = buildDeath(person)
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
            gender = person.gender
            marriedLastName = person.marriedLastName
            birthPlace = person.birthPlace
            birthDate = personConverter.map(person.birthDate)
            death = buildDeath(person)
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
        val person = personConverter.map(entity)
        val marriages = marriageRepository.findAllBySpouseA_IdOrSpouseB_Id(id, id)
            .map { marriage ->
                PersonMarriage(
                    id = marriage.id,
                    husbandId = marriage.spouseA.id,
                    wifeId = marriage.spouseB.id,
                    status = if (marriage.divorce == null) {
                        MarriageStatus.ACTIVE
                    } else {
                        MarriageStatus.FORMER
                    },
                    registrationDate = personConverter.map(marriage.registrationDate),
                    registrationPlace = marriage.registrationPlace,
                    divorceDate = personConverter.map(marriage.divorce?.divorceDate),
                )
            }
        return person.copy(marriages = marriages)
    }

    /**
     * Удалить человека в БД
     */
    fun deleteById(id: Long) {
        personRepository.deleteById(id)
    }

    private fun buildDeath(person: Person): DeathEntity? {
        if (person.deathDate == null && person.deathPlace == null) {
            return null
        }
        return DeathEntity(
            deathDate = personConverter.map(person.deathDate),
            deathPlace = person.deathPlace,
        )
    }

}
