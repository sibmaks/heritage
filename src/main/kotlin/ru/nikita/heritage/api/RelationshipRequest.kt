package ru.nikita.heritage.api

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
data class RelationshipRequest(
    val spouseId: Long,
    val relationshipType: RelationshipType = RelationshipType.MARRIAGE,
    val registrationDate: FlexibleDate? = null,
    val registrationPlace: String? = null,
    val divorceDate: FlexibleDate? = null,
)
