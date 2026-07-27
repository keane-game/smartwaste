package sn.smartwaste.collect.territory.domain.model;

import org.hibernate.annotations.UuidGenerator;
import sn.smartwaste.collect.shared.infrastructure.persistence.UuidV7Generator;

import java.util.UUID;

import sonaged.collecte.master.model.AbstractAuditingEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "REGION")
public class RegionEntity extends AbstractAuditingEntity<UUID> {
    @Id
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column(name = "RegionId")
    UUID regionId;

    @Column(name = "Name")
    String name;

    @Column(name = "Code")
    String code;

    @OneToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "geometryId", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    GeometryEntity geometry;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "region")
    @JsonIgnore
    @ToString.Exclude
    List<DepartmentEntity> departments;

}
