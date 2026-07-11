package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Depotoir;
import sonaged.collecte.master.model.DepotoirEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface DepotoirMapper {

    DepotoirMapper DETMP = Mappers.getMapper(DepotoirMapper.class);

    Depotoir asDto(DepotoirEntity depot);

    DepotoirEntity asModel(Depotoir depot);

    List<Depotoir> asListDto(List<DepotoirEntity> depots);
}
