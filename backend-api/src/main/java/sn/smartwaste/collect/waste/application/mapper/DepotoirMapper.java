package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.Depotoir;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface DepotoirMapper {

    DepotoirMapper DETMP = Mappers.getMapper(DepotoirMapper.class);

    Depotoir asDto(DepotoirEntity depot);

    /**
     * Le statut de suppression logique n'est JAMAIS repris du DTO.
     *
     * <p>{@code Depotoir} expose {@code deletionStatus} pour la lecture, mais MapStruct s'en
     * servait aussi en écriture : sur une création, le champ arrive à {@code null} et le
     * {@code setDeletionStatus(null)} généré écrasait la valeur {@code ACTIVE} initialisée par
     * {@link sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity}. La colonne étant
     * {@code NOT NULL}, tout {@code POST /v1/depotoirs} finissait en 500 — la création de point de
     * collecte était totalement inopérante.
     *
     * <p>Les ignorer est aussi la bonne règle sur le fond : l'état de suppression logique est piloté
     * par {@code SoftDeleteService}, pas par une charge utile cliente, qui pourrait sinon restaurer
     * ou marquer un enregistrement au passage.
     */
    @Mapping(target = "deletionStatus", ignore = true)
    @Mapping(target = "deletionRequestedAt", ignore = true)
    DepotoirEntity asModel(Depotoir depot);

    List<Depotoir> asListDto(List<DepotoirEntity> depots);
}
