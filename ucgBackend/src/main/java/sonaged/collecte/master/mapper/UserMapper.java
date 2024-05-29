package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.dto.UserResponse;
import sonaged.collecte.master.model.UserEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface UserMapper {

    UserMapper UMP = Mappers.getMapper(UserMapper.class);

    User asDto(User user);

    UserEntity asModel(User user);

    User asDto(UserResponse user);

    //List<UserDto> listModelToDto(List<User> users);

    List<UserResponse> listModelToDto(List<User> users);
}
