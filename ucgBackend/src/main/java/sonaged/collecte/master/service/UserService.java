package sonaged.collecte.master.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import sonaged.collecte.master.dto.UserDto;
import sonaged.collecte.master.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    UserDto getOneUser(Long userId);

    List<UserDto> getAllUser();

    UserDto createOneUser(UserDto userDto);

    UserDto updateOneUser(Long userId, UserDto userDto);
    void deleteOneUser(Long userId);

    User loadUserByUsername(String username);

}
