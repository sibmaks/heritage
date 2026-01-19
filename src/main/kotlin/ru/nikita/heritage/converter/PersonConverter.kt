package ru.nikita.heritage.converter

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy
import ru.nikita.heritage.api.FlexibleDate
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
    fun map(entity: PersonEntity): Person

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mother", ignore = true)
    @Mapping(target = "father", ignore = true)
    fun map(person: Person): PersonEntity

    fun map(entity: FlexibleDateEntity?): FlexibleDate?

    fun map(date: FlexibleDate?): FlexibleDateEntity?
}
