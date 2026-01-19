package ru.nikita.heritage.converter

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy
import ru.nikita.heritage.api.FlexibleDate
import ru.nikita.heritage.api.FlexibleDateType
import ru.nikita.heritage.api.Person
import ru.nikita.heritage.entity.FlexibleDateEntity
import ru.nikita.heritage.entity.PersonEntity

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    typeConversionPolicy = ReportingPolicy.WARN,
    nullValueMappingStrategy = org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT
)
interface PersonConverter {
    @Mapping(target = "motherId", source = "mother.id")
    @Mapping(target = "fatherId", source = "father.id")
    @Mapping(target = "marriages", expression = "java(java.util.Collections.emptyList())")
    @Mapping(target = "deathDate", source = "death.deathDate")
    @Mapping(target = "deathPlace", source = "death.deathPlace")
    fun map(entity: PersonEntity): Person

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mother", ignore = true)
    @Mapping(target = "father", ignore = true)
    @Mapping(target = "death", ignore = true)
    @Mapping(target = "externalUuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    fun map(person: Person): PersonEntity

    fun map(entity: FlexibleDateEntity?): FlexibleDate? {
        if (entity == null) {
            return null
        }
        val inferredType = entity.type ?: when {
            entity.startDate != null || entity.endDate != null -> FlexibleDateType.BETWEEN
            entity.date != null -> FlexibleDateType.EXACT
            else -> FlexibleDateType.EXACT
        }
        return FlexibleDate(
            type = inferredType,
            date = entity.date,
            startDate = entity.startDate,
            endDate = entity.endDate,
        )
    }

    fun map(date: FlexibleDate?): FlexibleDateEntity? {
        if (date == null) {
            return null
        }
        return FlexibleDateEntity(
            type = date.type,
            date = date.date,
            startDate = date.startDate,
            endDate = date.endDate,
        )
    }
}
