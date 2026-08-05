package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.Image;
import sn.smartwaste.collect.waste.domain.model.ImageEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface ImageMapper {

    ImageMapper IMG = Mappers.getMapper(ImageMapper.class );
    Image asDto(ImageEntity imageEntity);
    ImageEntity asModel(Image image);
}