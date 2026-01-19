package ru.nikita.heritage.entity

import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

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
    var lastName: String?,
    @Column(name = "first_name") // колонка имя
    var firstName: String?,
    @Column(name = "gender") // колонка пол
    var gender: Boolean,
    @Column(name = "married_last_name") // колонка фамилия после замужества
    var marriedLastName: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birth_place_id")
    var birthPlace: PlaceEntity? = null,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "type", column = Column(name = "birth_date_type")),
        AttributeOverride(name = "date", column = Column(name = "birth_date_value")),
        AttributeOverride(name = "startDate", column = Column(name = "birth_date_start")),
        AttributeOverride(name = "endDate", column = Column(name = "birth_date_end")),
    )
    var birthDate: FlexibleDateEntity? = null,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "death_id")
    var death: DeathEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    var mother: PersonEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    var father: PersonEntity? = null,
    @Column(name = "external_uuid")
    var externalUuid: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
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
