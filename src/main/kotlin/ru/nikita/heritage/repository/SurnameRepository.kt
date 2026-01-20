package ru.nikita.heritage.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.nikita.heritage.entity.SurnameEntity
import java.util.Optional

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
interface SurnameRepository : JpaRepository<SurnameEntity, Long> {
    fun findByValue(value: String): Optional<SurnameEntity>
}
