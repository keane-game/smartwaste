package ucg.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.MobilieUrbainDto;
import ucg.collecte.master.model.MobilieUrbain;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MobilieUrbainMapper {
    MobilieUrbainMapper MUMP = Mappers.getMapper(MobilieUrbainMapper.class);

    MobilieUrbainDto modelToDto(MobilieUrbain mobilieUrbain);

    MobilieUrbain dtoToModel (MobilieUrbainDto mobilieUrbainDto);

    List<MobilieUrbainDto> listModelToDto(List<MobilieUrbain> mobilieUrbains);
}
