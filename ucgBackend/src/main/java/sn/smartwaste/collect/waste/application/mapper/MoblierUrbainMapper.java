package sn.smartwaste.collect.waste.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.waste.application.dto.MoblierUrbain;
import sn.smartwaste.collect.waste.domain.model.MoblierUrbainEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface MoblierUrbainMapper {

    MoblierUrbainMapper MUMP = Mappers.getMapper(MoblierUrbainMapper.class);

    MoblierUrbain asDto(MoblierUrbainEntity moblierUrbain);

    MoblierUrbainEntity asModel(MoblierUrbain moblierUrbain);

    List<MoblierUrbain> asListDto(List<MoblierUrbainEntity> moblierUrbains);
}
