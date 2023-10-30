package ucg.collecte.master.service;

import ucg.collecte.master.dto.UserDto;
import ucg.collecte.master.model.User;

import java.util.List;

public interface UserService {
    UserDto getOneUser(Long userId);

    List<UserDto> getAllUser();

    UserDto createOneUser(UserDto userDto);

    UserDto updateOneUser(Long userId, UserDto userDto);
    void deleteOneUser(Long userId);
}
