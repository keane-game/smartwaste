package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.Depotoir;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface DepotoirMapper {

    DepotoirMapper DETMP = Mappers.getMapper(DepotoirMapper.class);

    Depotoir asDto(DepotoirEntity depot);

    DepotoirEntity asModel(Depotoir depot);

    List<Depotoir> asListDto(List<DepotoirEntity> depots);
}
