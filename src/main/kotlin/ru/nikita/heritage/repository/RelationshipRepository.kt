package ru.nikita.heritage.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.nikita.heritage.entity.RelationshipEntity

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
interface RelationshipRepository : JpaRepository<RelationshipEntity, Long> {
    fun findAllBySpouseA_IdOrSpouseB_Id(spouseAId: Long, spouseBId: Long): List<RelationshipEntity>
}
