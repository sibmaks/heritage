package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class PersonRelationship(
    val id: Long,
    val husbandId: Long,
    val wifeId: Long,
    val relationshipType: RelationshipType,
    val status: RelationshipStatus,
    val registrationDate: FlexibleDate? = null,
    val registrationPlace: String? = null,
    val divorceDate: FlexibleDate? = null,
)
