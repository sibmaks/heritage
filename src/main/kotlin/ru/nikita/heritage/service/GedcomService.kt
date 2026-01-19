package ru.nikita.heritage.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikita.heritage.api.FlexibleDateType
import ru.nikita.heritage.api.GedcomImportResult
import ru.nikita.heritage.entity.FlexibleDateEntity
import ru.nikita.heritage.entity.MarriageEntity
import ru.nikita.heritage.entity.PersonEntity
import ru.nikita.heritage.repository.MarriageRepository
import ru.nikita.heritage.repository.PersonRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Service
class GedcomService(
    val personRepository: PersonRepository,
    val marriageRepository: MarriageRepository,
) {
    private val gedcomDateFormatter = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH)
    private val yearFormatter = DateTimeFormatter.ofPattern("uuuu", Locale.ENGLISH)

    fun exportGedcom(): String {
        val persons = personRepository.findAll()
        val marriages = marriageRepository.findAll()
        val families = buildFamilies(persons, marriages)
        val builder = StringBuilder()
        builder.appendLine("0 HEAD")
        builder.appendLine("1 SOUR heritage")
        builder.appendLine("1 GEDC")
        builder.appendLine("2 VERS 5.5.1")
        builder.appendLine("1 CHAR UTF-8")
        persons.forEach { person ->
            builder.appendLine("0 @I${person.id}@ INDI")
            builder.appendLine("1 NAME ${formatName(person)}")
            person.marriedLastName?.let { builder.appendLine("1 _MARN $it") }
            appendEvent(builder, "BIRT", person.birthDate, person.birthPlace)
            appendEvent(builder, "DEAT", person.deathDate, person.deathPlace)
        }
        families.forEachIndexed { index, family ->
            builder.appendLine("0 @F${index + 1}@ FAM")
            family.husbandId?.let { builder.appendLine("1 HUSB @I$it@") }
            family.wifeId?.let { builder.appendLine("1 WIFE @I$it@") }
            family.childrenIds.forEach { childId ->
                builder.appendLine("1 CHIL @I$childId@")
            }
            family.marriage?.let { marriage ->
                if (marriage.registrationDate != null || marriage.registrationPlace != null) {
                    builder.appendLine("1 MARR")
                    appendEventDetails(builder, marriage.registrationDate, marriage.registrationPlace)
                }
                if (marriage.divorceDate != null) {
                    builder.appendLine("1 DIV")
                    appendEventDetails(builder, marriage.divorceDate, null)
                }
            }
        }
        builder.appendLine("0 TRLR")
        return builder.toString()
    }

    @Transactional
    fun importGedcom(content: String): GedcomImportResult {
        val persons = LinkedHashMap<String, GedcomPersonData>()
        val families = LinkedHashMap<String, GedcomFamilyData>()
        var currentPerson: GedcomPersonData? = null
        var currentFamily: GedcomFamilyData? = null
        var currentEvent: String? = null
        var currentContext: String? = null

        content.lineSequence().forEach { line ->
            val parsed = parseLine(line) ?: return@forEach
            val (level, xref, tag, value) = parsed
            when (level) {
                0 -> {
                    currentPerson = null
                    currentFamily = null
                    currentEvent = null
                    currentContext = null
                    if (tag == "INDI" && xref != null) {
                        currentPerson = persons.getOrPut(xref) { GedcomPersonData(xref) }
                        currentContext = "PERSON"
                    } else if (tag == "FAM" && xref != null) {
                        currentFamily = families.getOrPut(xref) { GedcomFamilyData(xref) }
                        currentContext = "FAMILY"
                    }
                }
                1 -> {
                    currentEvent = null
                    when (currentContext) {
                        "PERSON" -> handlePersonLevel1(currentPerson, tag, value).also { event ->
                            currentEvent = event
                        }
                        "FAMILY" -> handleFamilyLevel1(currentFamily, tag, value).also { event ->
                            currentEvent = event
                        }
                    }
                }
                2 -> {
                    when (currentContext) {
                        "PERSON" -> applyPersonLevel2(currentPerson, currentEvent, tag, value)
                        "FAMILY" -> applyFamilyLevel2(currentFamily, currentEvent, tag, value)
                    }
                }
            }
        }

        val personEntities = persons.values.map { data ->
            PersonEntity(
                lastName = data.lastName ?: "Unknown",
                firstName = data.firstName ?: "Unknown",
                middleName = data.middleName,
                marriedLastName = data.marriedLastName,
                birthPlace = data.birthPlace,
                deathPlace = data.deathPlace,
                birthDate = data.birthDate,
                deathDate = data.deathDate,
            )
        }
        val savedPersons = personRepository.saveAll(personEntities)
        val personMap = LinkedHashMap<String, PersonEntity>()
        persons.keys.forEachIndexed { index, key ->
            personMap[key] = savedPersons[index]
        }

        var marriagesCount = 0
        families.values.forEach { family ->
            val husband = family.husbandRef?.let { personMap[it] }
            val wife = family.wifeRef?.let { personMap[it] }
            family.childrenRefs.forEach { childRef ->
                val child = personMap[childRef] ?: return@forEach
                if (wife != null) {
                    child.mother = wife
                }
                if (husband != null) {
                    child.father = husband
                }
                personRepository.save(child)
            }
            if (husband != null && wife != null) {
                marriageRepository.save(
                    MarriageEntity(
                        spouseA = husband,
                        spouseB = wife,
                        registrationDate = family.marriageDate,
                        registrationPlace = family.marriagePlace,
                        divorceDate = family.divorceDate,
                    )
                )
                marriagesCount += 1
            }
        }

        return GedcomImportResult(
            persons = savedPersons.size,
            marriages = marriagesCount,
            families = families.size,
        )
    }

    private fun formatName(person: PersonEntity): String {
        val given = listOfNotNull(person.firstName, person.middleName).joinToString(" ")
        return "$given /${person.lastName}/"
    }

    private fun appendEvent(builder: StringBuilder, tag: String, date: FlexibleDateEntity?, place: String?) {
        if (date == null && place == null) {
            return
        }
        builder.appendLine("1 $tag")
        appendEventDetails(builder, date, place)
    }

    private fun appendEventDetails(builder: StringBuilder, date: FlexibleDateEntity?, place: String?) {
        date?.let { builder.appendLine("2 DATE ${formatFlexibleDate(it)}") }
        place?.let { builder.appendLine("2 PLAC $it") }
    }

    private fun formatFlexibleDate(date: FlexibleDateEntity): String {
        return when (date.type) {
            FlexibleDateType.EXACT -> date.date?.format(gedcomDateFormatter).orEmpty()
            FlexibleDateType.APPROXIMATE -> "ABT ${formatDateOrYear(date.date)}"
            FlexibleDateType.BETWEEN -> "BET ${formatDateOrYear(date.startDate)} AND ${formatDateOrYear(date.endDate)}"
            null -> date.date?.format(gedcomDateFormatter).orEmpty()
        }
    }

    private fun formatDateOrYear(date: LocalDate?): String {
        if (date == null) {
            return ""
        }
        return if (date.monthValue == 1 && date.dayOfMonth == 1) {
            date.format(yearFormatter)
        } else {
            date.format(gedcomDateFormatter)
        }
    }

    private fun parseLine(line: String): ParsedLine? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val match = Regex("""(\d+)\s+(?:(@[^@]+@)\s+)?([A-Z0-9_]+)(?:\s+(.*))?""").matchEntire(trimmed)
            ?: return null
        val level = match.groupValues[1].toInt()
        val xref = match.groupValues[2].ifEmpty { null }?.trim('@')
        val tag = match.groupValues[3]
        val value = match.groupValues[4].ifEmpty { null }
        return ParsedLine(level, xref, tag, value)
    }

    private fun handlePersonLevel1(person: GedcomPersonData?, tag: String, value: String?): String? {
        if (person == null) {
            return null
        }
        return when (tag) {
            "NAME" -> {
                parseName(value).let { (first, middle, last) ->
                    person.firstName = first
                    person.middleName = middle
                    person.lastName = last
                }
                null
            }
            "_MARN" -> {
                person.marriedLastName = value?.trim()
                null
            }
            "BIRT", "DEAT" -> tag
            else -> null
        }
    }

    private fun handleFamilyLevel1(family: GedcomFamilyData?, tag: String, value: String?): String? {
        if (family == null) {
            return null
        }
        return when (tag) {
            "HUSB" -> {
                family.husbandRef = value?.trim()?.trim('@')
                null
            }
            "WIFE" -> {
                family.wifeRef = value?.trim()?.trim('@')
                null
            }
            "CHIL" -> {
                value?.trim()?.trim('@')?.let { family.childrenRefs.add(it) }
                null
            }
            "MARR", "DIV" -> tag
            else -> null
        }
    }

    private fun applyPersonLevel2(person: GedcomPersonData?, event: String?, tag: String, value: String?) {
        if (person == null || event == null) {
            return
        }
        when (event) {
            "BIRT" -> applyEventToPerson(person, tag, value, isBirth = true)
            "DEAT" -> applyEventToPerson(person, tag, value, isBirth = false)
        }
    }

    private fun applyFamilyLevel2(family: GedcomFamilyData?, event: String?, tag: String, value: String?) {
        if (family == null || event == null) {
            return
        }
        when (event) {
            "MARR" -> applyEventToFamily(family, tag, value, isMarriage = true)
            "DIV" -> applyEventToFamily(family, tag, value, isMarriage = false)
        }
    }

    private fun applyEventToPerson(person: GedcomPersonData, tag: String, value: String?, isBirth: Boolean) {
        when (tag) {
            "DATE" -> {
                val date = parseFlexibleDate(value)
                if (isBirth) {
                    person.birthDate = date
                } else {
                    person.deathDate = date
                }
            }
            "PLAC" -> {
                if (isBirth) {
                    person.birthPlace = value?.trim()
                } else {
                    person.deathPlace = value?.trim()
                }
            }
        }
    }

    private fun applyEventToFamily(family: GedcomFamilyData, tag: String, value: String?, isMarriage: Boolean) {
        when (tag) {
            "DATE" -> {
                val date = parseFlexibleDate(value)
                if (isMarriage) {
                    family.marriageDate = date
                } else {
                    family.divorceDate = date
                }
            }
            "PLAC" -> if (isMarriage) {
                family.marriagePlace = value?.trim()
            }
        }
    }

    private fun parseName(value: String?): Triple<String?, String?, String?> {
        if (value.isNullOrBlank()) {
            return Triple(null, null, null)
        }
        val lastName = Regex("/([^/]+)/").find(value)?.groupValues?.get(1)
        val given = value.replace(Regex("/[^/]+/"), "").trim()
        val parts = given.split(" ").filter { it.isNotBlank() }
        val first = parts.firstOrNull()
        val middle = if (parts.size > 1) parts.drop(1).joinToString(" ") else null
        return Triple(first, middle, lastName)
    }

    private fun parseFlexibleDate(value: String?): FlexibleDateEntity? {
        val text = value?.trim().orEmpty()
        if (text.isBlank()) {
            return null
        }
        if (text.startsWith("ABT ")) {
            val date = parseDate(text.removePrefix("ABT ").trim())
            return FlexibleDateEntity(type = FlexibleDateType.APPROXIMATE, date = date)
        }
        if (text.startsWith("BET ")) {
            val parts = text.removePrefix("BET ").split("AND")
            val start = parseDate(parts.getOrNull(0)?.trim().orEmpty())
            val end = parseDate(parts.getOrNull(1)?.trim().orEmpty())
            return FlexibleDateEntity(type = FlexibleDateType.BETWEEN, startDate = start, endDate = end)
        }
        val date = parseDate(text)
        return FlexibleDateEntity(type = FlexibleDateType.EXACT, date = date)
    }

    private fun parseDate(value: String): LocalDate? {
        if (value.isBlank()) {
            return null
        }
        return try {
            LocalDate.parse(value, gedcomDateFormatter)
        } catch (ex: DateTimeParseException) {
            try {
                LocalDate.parse(value, yearFormatter).withMonth(1).withDayOfMonth(1)
            } catch (exYear: DateTimeParseException) {
                null
            }
        }
    }

    private fun buildFamilies(
        persons: List<PersonEntity>,
        marriages: List<MarriageEntity>
    ): List<FamilyExport> {
        val familiesByPair = LinkedHashMap<String, FamilyExport>()
        marriages.forEach { marriage ->
            val pairKey = pairKey(marriage.spouseA.id, marriage.spouseB.id)
            familiesByPair[pairKey] = FamilyExport(
                husbandId = marriage.spouseA.id,
                wifeId = marriage.spouseB.id,
                childrenIds = LinkedHashSet(),
                marriage = marriage,
            )
        }
        persons.forEach { person ->
            val motherId = person.mother?.id
            val fatherId = person.father?.id
            if (motherId != null || fatherId != null) {
                val pairKey = pairKey(motherId, fatherId)
                val family = familiesByPair.getOrPut(pairKey) {
                    FamilyExport(
                        husbandId = fatherId,
                        wifeId = motherId,
                        childrenIds = LinkedHashSet(),
                        marriage = null,
                    )
                }
                family.childrenIds.add(person.id)
            }
        }
        return familiesByPair.values.toList()
    }

    private fun pairKey(a: Long?, b: Long?): String {
        val ids = listOfNotNull(a, b).sorted()
        return ids.joinToString("-")
    }

    private data class ParsedLine(
        val level: Int,
        val xref: String?,
        val tag: String,
        val value: String?,
    )

    private data class GedcomPersonData(
        val id: String,
        var firstName: String? = null,
        var lastName: String? = null,
        var middleName: String? = null,
        var marriedLastName: String? = null,
        var birthDate: FlexibleDateEntity? = null,
        var birthPlace: String? = null,
        var deathDate: FlexibleDateEntity? = null,
        var deathPlace: String? = null,
    )

    private data class GedcomFamilyData(
        val id: String,
        var husbandRef: String? = null,
        var wifeRef: String? = null,
        val childrenRefs: MutableList<String> = mutableListOf(),
        var marriageDate: FlexibleDateEntity? = null,
        var marriagePlace: String? = null,
        var divorceDate: FlexibleDateEntity? = null,
    )

    private data class FamilyExport(
        val husbandId: Long?,
        val wifeId: Long?,
        val childrenIds: MutableSet<Long>,
        val marriage: MarriageEntity?,
    )
}
