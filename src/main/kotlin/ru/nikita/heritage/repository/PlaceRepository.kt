package ru.nikita.heritage.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.nikita.heritage.entity.PlaceEntity
import java.util.Optional

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
interface PlaceRepository : JpaRepository<PlaceEntity, Long> {
    fun findByName(name: String): Optional<PlaceEntity>
}
