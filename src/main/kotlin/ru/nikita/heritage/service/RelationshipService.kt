package ru.nikita.heritage.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikita.heritage.api.*
import ru.nikita.heritage.converter.PersonConverter
import ru.nikita.heritage.entity.DivorceEntity
import ru.nikita.heritage.entity.RelationshipEntity
import ru.nikita.heritage.entity.PlaceEntity
import ru.nikita.heritage.repository.RelationshipRepository
import ru.nikita.heritage.repository.PersonRepository
import ru.nikita.heritage.repository.PlaceRepository
import java.util.*

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Service
class RelationshipService(
    val personRepository: PersonRepository,
    val relationshipRepository: RelationshipRepository,
    val placeRepository: PlaceRepository,
    val personConverter: PersonConverter,
) {

    @Transactional
    fun setParents(personId: Long, request: ParentsRequest) {
        val person = personRepository.findByIdLocked(personId)
            .orElseThrow { IllegalArgumentException("Человек с id: $personId не найден") }
        val mother = request.motherId?.let { id ->
            personRepository.findById(id).orElseThrow { IllegalArgumentException("Мать с id: $id не найдена") }
        }
        val father = request.fatherId?.let { id ->
            personRepository.findById(id).orElseThrow { IllegalArgumentException("Отец с id: $id не найден") }
        }
        person.mother = mother
        person.father = father
        personRepository.save(person)
    }

    @Transactional
    fun addRelationship(personId: Long, request: RelationshipRequest): Long {
        val person = personRepository.findById(personId)
            .orElseThrow { IllegalArgumentException("Человек с id: $personId не найден") }
        val spouse = personRepository.findById(request.spouseId)
            .orElseThrow { IllegalArgumentException("Супруг с id: ${request.spouseId} не найден") }
        val relationship = RelationshipEntity(
            spouseA = person,
            spouseB = spouse,
            relationshipType = request.relationshipType,
            registrationDate = personConverter.map(request.registrationDate),
            registrationPlace = buildPlace(request.registrationPlace),
            divorce = request.divorceDate?.let { date ->
                DivorceEntity(divorceDate = personConverter.map(date))
            },
        )
        return relationshipRepository.save(relationship).id
    }

    fun getRelatives(personId: Long, depth: Int): RelativesResponse {
        if (depth < 1) {
            return RelativesResponse(emptyList(), emptyList())
        }
        val visited = mutableSetOf(personId)
        val queue: ArrayDeque<Pair<Long, Int>> = ArrayDeque()
        val persons = mutableListOf<RelativePerson>()
        val relationships = LinkedHashMap<Long, PersonRelationship>()
        queue.add(personId to 0)
        while (queue.isNotEmpty()) {
            val (currentId, currentDepth) = queue.removeFirst()
            if (currentDepth >= depth) {
                continue
            }
            val neighbors = getNeighbors(currentId)
            for (neighbor in neighbors) {
                if (visited.add(neighbor.id)) {
                    val relative = buildRelative(neighbor)
                    persons.add(relative)
                    val relativeRelationships = relationshipRepository
                        .findAllBySpouseA_IdOrSpouseB_Id(relative.id, relative.id)
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
                    relativeRelationships.forEach { relationship ->
                        relationships.putIfAbsent(relationship.id, relationship)
                    }
                    queue.add(neighbor.id to currentDepth + 1)
                }
            }
        }
        return RelativesResponse(persons = persons, relationships = relationships.values.toList())
    }

    private fun getNeighbors(personId: Long): Set<ru.nikita.heritage.entity.PersonEntity> {
        val person = personRepository.findById(personId)
            .orElseThrow { IllegalArgumentException("Человек с id: $personId не найден") }
        val neighbors = LinkedHashSet<ru.nikita.heritage.entity.PersonEntity>()
        person.mother?.let { neighbors.add(it) }
        person.father?.let { neighbors.add(it) }
        val children = personRepository.findAllByMother_IdOrFather_Id(personId, personId)
        neighbors.addAll(children)
        return neighbors
    }

    private fun buildRelative(person: ru.nikita.heritage.entity.PersonEntity): RelativePerson {
        val base = personConverter.map(person)
        return RelativePerson(
            id = base.id ?: throw IllegalStateException("Relative person id is null"),
            lastName = base.lastName,
            firstName = base.firstName,
            gender = base.gender,
            marriedLastName = base.marriedLastName,
            birthDate = base.birthDate,
            deathDate = base.deathDate,
            motherId = base.motherId,
            fatherId = base.fatherId,
        )
    }

    private fun buildPlace(place: String?): PlaceEntity? {
        if (place.isNullOrBlank()) {
            return null
        }
        return placeRepository.findByName(place)
            .orElseGet { placeRepository.save(PlaceEntity(name = place)) }
    }
}
