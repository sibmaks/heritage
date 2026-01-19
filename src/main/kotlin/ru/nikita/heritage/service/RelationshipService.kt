package ru.nikita.heritage.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikita.heritage.api.MarriageRequest
import ru.nikita.heritage.api.ParentsRequest
import ru.nikita.heritage.api.Person
import ru.nikita.heritage.converter.PersonConverter
import ru.nikita.heritage.entity.DivorceEntity
import ru.nikita.heritage.entity.MarriageEntity
import ru.nikita.heritage.repository.MarriageRepository
import ru.nikita.heritage.repository.PersonRepository
import java.util.ArrayDeque

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Service
class RelationshipService(
    val personRepository: PersonRepository,
    val marriageRepository: MarriageRepository,
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
    fun addMarriage(personId: Long, request: MarriageRequest): Long {
        val person = personRepository.findById(personId)
            .orElseThrow { IllegalArgumentException("Человек с id: $personId не найден") }
        val spouse = personRepository.findById(request.spouseId)
            .orElseThrow { IllegalArgumentException("Супруг с id: ${request.spouseId} не найден") }
        val marriage = MarriageEntity(
            spouseA = person,
            spouseB = spouse,
            registrationDate = personConverter.map(request.registrationDate),
            registrationPlace = request.registrationPlace,
            divorce = request.divorceDate?.let { date ->
                DivorceEntity(divorceDate = personConverter.map(date))
            },
        )
        return marriageRepository.save(marriage).id
    }

    fun getRelatives(personId: Long, depth: Int): List<Person> {
        if (depth < 1) {
            return emptyList()
        }
        val visited = mutableSetOf(personId)
        val queue: ArrayDeque<Pair<Long, Int>> = ArrayDeque()
        val result = mutableListOf<Person>()
        queue.add(personId to 0)
        while (queue.isNotEmpty()) {
            val (currentId, currentDepth) = queue.removeFirst()
            if (currentDepth >= depth) {
                continue
            }
            val neighbors = getNeighbors(currentId)
            for (neighbor in neighbors) {
                if (visited.add(neighbor.id)) {
                    result.add(personConverter.map(neighbor))
                    queue.add(neighbor.id to currentDepth + 1)
                }
            }
        }
        return result
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
}
