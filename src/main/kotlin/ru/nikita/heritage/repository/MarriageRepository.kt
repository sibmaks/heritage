package ru.nikita.heritage.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.nikita.heritage.entity.MarriageEntity

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
interface MarriageRepository : JpaRepository<MarriageEntity, Long> {
    fun findAllBySpouseA_IdOrSpouseB_Id(spouseAId: Long, spouseBId: Long): List<MarriageEntity>
}
