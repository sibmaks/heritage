package ru.nikita.heritage.service

import jakarta.persistence.criteria.JoinType
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikita.heritage.api.*
import ru.nikita.heritage.converter.PersonConverter
import ru.nikita.heritage.entity.*
import ru.nikita.heritage.repository.*

/**
 * Класс с логикой работы с людьми
 * @author sibmaks
 * @since 0.0.1
 */
@Service
class PersonService(
    val personRepository: PersonRepository,
    val relationshipRepository: RelationshipRepository,
    val placeRepository: PlaceRepository,
    val nameRepository: NameRepository,
    val surnameRepository: SurnameRepository,
    val personConverter: PersonConverter,
    @param:Value($$"${heritage.search.limit:20}")
    private val searchLimit: Int,
) {

    /**
     * Добавление человека в Базу данных
     */
    fun add(person: Person): Long {
        var entity = personConverter.map(person)
        entity.death = buildDeath(person)
        entity.birthPlace = buildPlace(person.birthPlace)
        entity.firstName = buildName(person.firstName)
        entity.lastName = buildSurname(person.lastName)
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
            gender = person.gender
            marriedLastName = person.marriedLastName
            biography = person.biography
            birthPlace = buildPlace(person.birthPlace)
            birthDate = personConverter.map(person.birthDate)
            death = buildDeath(person)
        }
        entity.firstName = buildName(person.firstName)
        entity.lastName = buildSurname(person.lastName)
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
        val relationships = relationshipRepository.findAllBySpouseA_IdOrSpouseB_Id(id, id)
            .map { relationship ->
                PersonRelationship(
                    id = relationship.id,
                    husbandId = relationship.spouseA.id,
                    wifeId = relationship.spouseB.id,
                    relationshipType = relationship.relationshipType,
                    status = if (relationship.divorce == null) {
                        RelationshipStatus.ACTIVE
                    } else {
                        RelationshipStatus.FORMER
                    },
                    registrationDate = personConverter.map(relationship.registrationDate),
                    registrationPlace = relationship.registrationPlace?.name,
                    divorceDate = personConverter.map(relationship.divorce?.divorceDate),
                )
            }
        return person.copy(relationships = relationships)
    }

    /**
     * Удалить человека в БД
     */
    fun deleteById(id: Long) {
        personRepository.deleteById(id)
    }

    fun searchByName(query: String): PersonSearchResponse {
        val terms = query.split(" ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
        if (terms.isEmpty()) {
            return PersonSearchResponse(persons = emptyList(), hasMore = false)
        }
        val specification = Specification<PersonEntity> { root, criteriaQuery, criteriaBuilder ->
            criteriaQuery?.distinct(true)
            val lastNameJoin = root.join<PersonEntity, SurnameEntity>("lastName", JoinType.LEFT)
            val firstNameJoin = root.join<PersonEntity, NameEntity>("firstName", JoinType.LEFT)
            val predicates = terms.map { term ->
                val likeValue = "%$term%"
                val lastNameValue = criteriaBuilder.lower(criteriaBuilder.coalesce(lastNameJoin.get("value"), ""))
                val firstNameValue = criteriaBuilder.lower(criteriaBuilder.coalesce(firstNameJoin.get("value"), ""))
                val marriedLastNameValue = criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("marriedLastName"), ""))
                criteriaBuilder.or(
                    criteriaBuilder.like(lastNameValue, likeValue),
                    criteriaBuilder.like(firstNameValue, likeValue),
                    criteriaBuilder.like(marriedLastNameValue, likeValue),
                )
            }
            criteriaBuilder.and(*predicates.toTypedArray())
        }
        val pageRequest = PageRequest.of(0, searchLimit + 1)
        val results = personRepository.findAll(specification, pageRequest).content
        val hasMore = results.size > searchLimit
        val items = results.take(searchLimit).map { entity ->
            val fullName = buildFullName(
                lastName = entity.lastName?.value,
                firstName = entity.firstName?.value,
                marriedLastName = entity.marriedLastName
            )
            PersonSearchItem(
                id = entity.id,
                fullName = fullName,
                gender = entity.gender,
                birthDate = personConverter.map(entity.birthDate),
                deathDate = personConverter.map(entity.death?.deathDate),
            )
        }
        return PersonSearchResponse(persons = items, hasMore = hasMore)
    }

    private fun buildDeath(person: Person): DeathEntity? {
        if (person.deathDate == null && person.deathPlace == null) {
            return null
        }
        return DeathEntity(
            deathDate = personConverter.map(person.deathDate),
            deathPlace = buildPlace(person.deathPlace),
        )
    }

    private fun buildPlace(place: String?): PlaceEntity? {
        if (place.isNullOrBlank()) {
            return null
        }
        return placeRepository.findByName(place)
            .orElseGet { placeRepository.save(PlaceEntity(name = place)) }
    }

    private fun buildName(name: String?): NameEntity? {
        if (name.isNullOrBlank()) {
            return null
        }
        return nameRepository.findByValue(name)
            .orElseGet { nameRepository.save(NameEntity(value = name)) }
    }

    private fun buildSurname(surname: String?): SurnameEntity? {
        if (surname.isNullOrBlank()) {
            return null
        }
        return surnameRepository.findByValue(surname)
            .orElseGet { surnameRepository.save(SurnameEntity(value = surname)) }
    }

    private fun buildFullName(
        lastName: String?,
        firstName: String?,
        marriedLastName: String?,
    ): String {
        val parts = listOfNotNull(
            lastName?.takeIf { it.isNotBlank() },
            firstName?.takeIf { it.isNotBlank() },
        )
        val baseName = parts.joinToString(" ").ifBlank { "—" }
        return if (marriedLastName.isNullOrBlank()) {
            baseName
        } else {
            "$baseName (${marriedLastName.trim()})"
        }
    }
}
