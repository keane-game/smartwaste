package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.TypeDepotoir;
import sn.smartwaste.collect.waste.domain.model.TypeDepotoirEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface TypeDepotoirMapper {

    TypeDepotoirMapper TDMP = Mappers.getMapper(TypeDepotoirMapper.class);

    TypeDepotoir asDto(TypeDepotoirEntity typeDepotoir);

    TypeDepotoirEntity asModel(TypeDepotoir typeDepotoir);

    List<TypeDepotoir> asListDto(List<TypeDepotoirEntity> typeDepotoirs);
}
