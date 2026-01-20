package ru.nikita.heritage.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.web.bind.annotation.*
import ru.nikita.heritage.api.*
import ru.nikita.heritage.service.GedcomService
import ru.nikita.heritage.service.PersonService
import ru.nikita.heritage.service.RelationshipService

/**
 * Принимаем запросы от фронтенда
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Tag(name = "Heritage API", description = "Endpoints for managing people and relationships.")
@RestController
@RequestMapping("/api/heritage/")
class HeritageRestController(
    val personService: PersonService,
    val relationshipService: RelationshipService,
    val gedcomService: GedcomService,
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

    /**
     * Установить маму/папу
     */
    @PutMapping("/{personId}/parents", consumes = [APPLICATION_JSON_VALUE])
    fun setParents(
        @PathVariable personId: Long,
        @RequestBody request: ParentsRequest
    ) {
        relationshipService.setParents(personId, request)
    }

    /**
     * Добавить брак
     */
    @PostMapping("/{personId}/marriages", consumes = [APPLICATION_JSON_VALUE])
    fun addMarriage(
        @PathVariable personId: Long,
        @RequestBody request: MarriageRequest
    ): Long {
        return relationshipService.addMarriage(personId, request)
    }

    /**
     * Получить прямых родственников до N-ого порядка
     */
    @GetMapping("/{personId}/relatives")
    fun getRelatives(
        @PathVariable personId: Long,
        @RequestParam depth: Int
    ): RelativesResponse {
        return relationshipService.getRelatives(personId, depth)
    }

    /**
     * Поиск человека по ФИО
     */
    @GetMapping("/persons/search")
    @Operation(
        summary = "Search persons by name",
        description = "Splits the query by spaces and matches each term against last name, first name, or married last name."
    )
    fun searchPersons(
        @Parameter(
            description = "Search string containing last name, first name, or married last name.",
            example = "Ivanova Maria"
        )
        @RequestParam query: String
    ): PersonSearchResponse {
        return personService.searchByName(query)
    }

    /**
     * Экспорт GEDCOM
     */
    @GetMapping("/gedcom/export", produces = [TEXT_PLAIN_VALUE])
    fun exportGedcom(): String {
        return gedcomService.exportGedcom()
    }

    /**
     * Импорт GEDCOM
     */
    @PostMapping("/gedcom/import", consumes = [TEXT_PLAIN_VALUE])
    fun importGedcom(@RequestBody content: String): GedcomImportResult {
        return gedcomService.importGedcom(content)
    }
}
