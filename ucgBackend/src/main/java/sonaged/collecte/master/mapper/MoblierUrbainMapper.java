package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.MoblierUrbainDto;
import sonaged.collecte.master.model.MoblierUrbain;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MoblierUrbainMapper {
    MoblierUrbainMapper MUMP = Mappers.getMapper(MoblierUrbainMapper.class);

    MoblierUrbainDto modelToDto(MoblierUrbain moblierUrbain);

    MoblierUrbain dtoToModel (MoblierUrbainDto moblierUrbainDto);

    List<MoblierUrbainDto> listModelToDto(List<MoblierUrbain> moblierUrbains);
}
