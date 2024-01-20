package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.TypeDepotoirDto;
import sonaged.collecte.master.model.TypeDepotoir;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TypeDepotoirMapper {

    TypeDepotoirMapper TDMP = Mappers.getMapper(TypeDepotoirMapper.class);

    TypeDepotoirDto modelToDto(TypeDepotoir typeDepotoir);

    TypeDepotoir dtoToModel (TypeDepotoirDto typeDepotoirDto);

    List<TypeDepotoirDto> listModelToDto(List<TypeDepotoir> typeDepotoirs);
}
