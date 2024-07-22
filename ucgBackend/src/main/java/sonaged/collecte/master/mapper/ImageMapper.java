package sonaged.collecte.master.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.Image;
import sonaged.collecte.master.model.ImageEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface ImageMapper {

    ImageMapper IMG = Mappers.getMapper(ImageMapper.class );
    Image asDto(ImageEntity imageEntity);
    ImageEntity asModel(Image image);
}