package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Commune;
import sonaged.collecte.master.model.CommuneEntity;

import java.util.List;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CommuneMapper {

    CommuneMapper COMP = Mappers.getMapper(CommuneMapper.class);

    // P1-7 : le DTO porte `departmentId` et non l'entité Department — MapStruct ne devine
    // pas la propriété imbriquée, la correspondance est donc explicite.
    @Mapping(source = "department.departmentId", target = "departmentId")
    Commune asDto(CommuneEntity commune);

    // Sens inverse : on ne reconstruit pas un Department à partir d'un simple identifiant
    // (ce serait une entité détachée incomplète). Le rattachement relève du service.
    @Mapping(target = "department", ignore = true)
    CommuneEntity asModel (Commune commune);

    List<Commune> asListDto(List<CommuneEntity> communes);
}
