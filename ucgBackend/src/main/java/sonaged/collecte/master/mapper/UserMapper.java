package sonaged.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sonaged.collecte.master.dto.UserDto;
import sonaged.collecte.master.model.User;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper UMP = Mappers.getMapper(UserMapper.class);

    UserDto modelToDto(User user);

    User dtoToModel (UserDto userDto);

    List<UserDto> listModelToDto(List<User> users);
}
