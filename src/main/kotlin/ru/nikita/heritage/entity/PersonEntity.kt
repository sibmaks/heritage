package ru.nikita.heritage.entity

import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides

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
    @Column(name = "gender") // колонка пол
    var gender: Boolean,
    @Column(name = "middle_name") // колонка отчество
    var middleName: String?,
    @Column(name = "married_last_name") // колонка фамилия после замужества
    var marriedLastName: String? = null,
    @Column(name = "birth_place") // колонка место рождения
    var birthPlace: String? = null,
    @Column(name = "death_place") // колонка место смерти
    var deathPlace: String? = null,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "type", column = Column(name = "birth_date_type")),
        AttributeOverride(name = "date", column = Column(name = "birth_date_value")),
        AttributeOverride(name = "startDate", column = Column(name = "birth_date_start")),
        AttributeOverride(name = "endDate", column = Column(name = "birth_date_end")),
    )
    var birthDate: FlexibleDateEntity? = null,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "type", column = Column(name = "death_date_type")),
        AttributeOverride(name = "date", column = Column(name = "death_date_value")),
        AttributeOverride(name = "startDate", column = Column(name = "death_date_start")),
        AttributeOverride(name = "endDate", column = Column(name = "death_date_end")),
    )
    var deathDate: FlexibleDateEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    var mother: PersonEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    var father: PersonEntity? = null,
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
