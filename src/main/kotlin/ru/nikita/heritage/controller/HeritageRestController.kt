package ru.nikita.heritage.controller

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import ru.nikita.heritage.api.Person
import ru.nikita.heritage.service.PersonService

/**
 * Принимаем запросы от фронтенда
 *
 * @author sibmaks
 * @since 0.0.1
 */
@RestController
@RequestMapping("/heritage/") // куда отправлять запросы
class HeritageRestController(
    val personService: PersonService,
) {

    /**
     * Добавить человека в базу данных
     */
    @PostMapping("/", consumes = [APPLICATION_JSON_VALUE])
    fun addPerson(@RequestBody person: Person): Long {
        return personService.add(person)
    }

    /**
     * Обновить человека в базе данных
     */
    @PutMapping("/{personId}", consumes = [APPLICATION_JSON_VALUE])
    fun updatePerson(
        @PathVariable personId: Long,
        @RequestBody person: Person
    ): Long {
        return personService.update(personId, person)
    }

    /**
     * Получить человека по идентификатору
     */
    @GetMapping("/{personId}")
    fun getPerson(@PathVariable personId: Long): Person {
        return personService.getById(personId)
    }

    /**
     * Удалить человека по идентификатору
     */
    @DeleteMapping("/{personId}")
    fun deletePerson(@PathVariable personId: Long) {
        personService.deleteById(personId)
    }

}