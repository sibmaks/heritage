package ru.nikita.heritage.entity

import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import org.hibernate.proxy.HibernateProxy

/**
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Entity
@Table(name = "marriage")
data class MarriageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spouse_a_id")
    var spouseA: PersonEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spouse_b_id")
    var spouseB: PersonEntity,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "type", column = Column(name = "registration_date_type")),
        AttributeOverride(name = "date", column = Column(name = "registration_date_value")),
        AttributeOverride(name = "startDate", column = Column(name = "registration_date_start")),
        AttributeOverride(name = "endDate", column = Column(name = "registration_date_end")),
    )
    var registrationDate: FlexibleDateEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_place_id")
    var registrationPlace: PlaceEntity? = null,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "divorce_id")
    var divorce: DivorceEntity? = null,
) {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as MarriageEntity

        return id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id )"
    }
}
