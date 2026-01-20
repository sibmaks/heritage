package ru.nikita.heritage.entity

import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Entity
@Table(name = "death")
data class DeathEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "type", column = Column(name = "death_date_type")),
        AttributeOverride(name = "date", column = Column(name = "death_date_value")),
        AttributeOverride(name = "startDate", column = Column(name = "death_date_start")),
        AttributeOverride(name = "endDate", column = Column(name = "death_date_end")),
    )
    var deathDate: FlexibleDateEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "death_place_id")
    var deathPlace: PlaceEntity? = null,
) {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as DeathEntity

        return id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id )"
    }
}
