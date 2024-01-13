package ucg.collecte.master;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.DepotoirDto;
import ucg.collecte.master.model.Depotoir;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepotoirMapper {

    DepotoirMapper DETMP = Mappers.getMapper(DepotoirMapper.class);

    DepotoirDto modelToDto(Depotoir depotoir);

    Depotoir dtoToModel (DepotoirDto depotoirDto);

    List<DepotoirDto> listModelToDto(List<Depotoir> depotoirs);
}
