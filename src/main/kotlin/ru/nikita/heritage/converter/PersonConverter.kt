package ru.nikita.heritage.converter

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy
import ru.nikita.heritage.api.Person
import ru.nikita.heritage.entity.PersonEntity

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    typeConversionPolicy = ReportingPolicy.WARN
)
interface PersonConverter {
    fun map(entity: PersonEntity): Person

    @Mapping(target = "id", ignore = true)
    fun map(person: Person): PersonEntity
}