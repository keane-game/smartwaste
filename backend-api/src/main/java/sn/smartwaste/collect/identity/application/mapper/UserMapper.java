package sn.smartwaste.collect.identity.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.domain.model.UserEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface UserMapper {

    UserMapper UMP = Mappers.getMapper(UserMapper.class);

    User asDto(UserEntity user);

    UserEntity asModel(User user);

    User asDto(User user);

    List<User> asListDto(List<UserEntity> users);
}
