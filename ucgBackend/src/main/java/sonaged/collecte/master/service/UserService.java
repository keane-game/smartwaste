package sonaged.collecte.master.service;

import sonaged.collecte.master.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getOneUser(Long userId);

    List<UserDto> getAllUser();

    UserDto createOneUser(UserDto userDto);

    UserDto updateOneUser(Long userId, UserDto userDto);
    void deleteOneUser(Long userId);
}
