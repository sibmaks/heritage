package ru.nikita.heritage.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.nikita.heritage.entity.PersonEntity
import java.util.*

/**
 * Класс для сохранения людей в БД
 * @author sibmaks
 * @since 0.0.1
 */
interface PersonRepository : JpaRepository<PersonEntity, Long>, JpaSpecificationExecutor<PersonEntity> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p from PersonEntity p where p.id = :personId")
    fun findByIdLocked(@Param("personId") personId: Long): Optional<PersonEntity>

    fun findAllByMother_IdOrFather_Id(motherId: Long, fatherId: Long): List<PersonEntity>
}
