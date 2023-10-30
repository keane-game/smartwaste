package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.UserDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.UserMapper;
import ucg.collecte.master.model.User;
import ucg.collecte.master.repository.UserRepository;
import ucg.collecte.master.service.UserService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl  implements UserService {

    private final UserRepository userRepository;


    /**
     * @param userId 
     * @return UserDto
     * @throws ResourceNotFoundException
     */
    @Override
    public UserDto getOneUser(Long userId) {
        User user  = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(userId)
                ));
        return UserMapper.UMP.modelToDto(user);
    }

    /**
     * @return  List<UserDto>
     *
     */
    @Override
    public List<UserDto> getAllUser() {
        List<User> userList = userRepository.findAll();
        return UserMapper.UMP.listModelToDto(userList);
    }

    /**
     * @param userDto 
     * @return UserDto
     */
    @Override
    public UserDto createOneUser(UserDto userDto) {
        User user = User.builder()
                .userName(userDto.getUserName())
                .userAddress(userDto.getUserAddress())
                .userPhone(userDto.getUserPhone())
                .userCode(userDto.getUserCode())
                .userEmail(userDto.getUserEmail())
                .authorities(userDto.getAuthorities())
                .build();
        User userSave = userRepository.save(user);
        return UserMapper.UMP.modelToDto(userSave);
    }

    /**
     * @param userId 
     * @param userDto
     * @return
     */
    @Override
    public UserDto updateOneUser(Long userId, UserDto userDto) {
        User existedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "User with id [%s] not found to update ".formatted(userId)
        ));
        if (!Objects.equals(existedUser.getUserId(), userDto.getUserId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        if (userDto.getUserEmail() != null){
            existedUser.setUserEmail(userDto.getUserEmail());
        }
        if (userDto.getUserName() != null){
            existedUser.setUserName(userDto.getUserName());
        }
        if (userDto.getUserCode() != null){
            existedUser.setUserCode(userDto.getUserCode());
        }
        if (userDto.getUserPhone() != null){
            existedUser.setUserPhone(userDto.getUserPhone());
        }
        if (userDto.getAuthorities() != null){
            existedUser.setAuthorities(userDto.getAuthorities());
        }
        User updateUser = userRepository.save(existedUser);
        return UserMapper.UMP.modelToDto(updateUser);
    }

    /**
     * @param userId 
     */
    @Override
    public void deleteOneUser(Long userId) {

        User user  = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found to delete".formatted(userId)
                ));
        userRepository.delete(user);
    }
}
