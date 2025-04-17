package ru.nikita.heritage.entity

import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDate

/**
 * Класс, который будем "хранить" в базе данных
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Entity // тут говорим, что это храним в БД
@Table(name = "person") // в какую таблицу сохранять
data class PersonEntity(
    @Id // Идентификатор
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @Column(name = "last_name") // колонка фамилия
    var lastName: String,
    @Column(name = "first_name") // колонка имя
    var firstName: String,
    @Column(name = "middle_name") // колонка отчество
    var middleName: String?,
    @Column(name = "birth_date") // колонка дата рождения
    var birthDate: LocalDate?,
    @Column(name = "death_date") // колонка дата смерти
    var deathDate: LocalDate?,
) {

    // это всё автоматически сгенерировано
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as PersonEntity

        return id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id )"
    }
}
