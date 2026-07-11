package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Commune;
import sonaged.collecte.master.model.CommuneEntity;

import java.util.List;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface CommuneMapper {

    CommuneMapper COMP = Mappers.getMapper(CommuneMapper.class);

    Commune asDto(CommuneEntity commune);

    CommuneEntity asModel (Commune commune);

    List<Commune> asListDto(List<CommuneEntity> communes);
}
