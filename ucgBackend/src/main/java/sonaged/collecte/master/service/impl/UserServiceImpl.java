package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.UserRepository;
import sonaged.collecte.master.dto.UserDto;
import sonaged.collecte.master.mapper.UserMapper;
import sonaged.collecte.master.model.User;
import sonaged.collecte.master.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl  implements UserService {

    private final UserRepository userRepository;


    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
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
                .userFirstname (userDto.getUserFirstname ())
                .userLastname (userDto.getUserLastname ())
                .userAddress(userDto.getUserAddress())
                .userPhone(userDto.getUserPhone())
                .userCode(userDto.getUserCode())
                .password(bCryptPasswordEncoder.encode(userDto.getPassword()))
                .userEmail(userDto.getUserEmail())
                .authority(userDto.getAuthority())
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
        if (userDto.getUserFirstname () != null){
            existedUser.setUserFirstname (userDto.getUserFirstname ());
        }

        if (userDto.getUserLastname () != null){
            existedUser.setUserLastname (userDto.getUserLastname ());
        }

        if (userDto.getUserCode() != null){
            existedUser.setUserCode(userDto.getUserCode());
        }
        if (userDto.getUserPhone() != null){
            existedUser.setUserPhone(userDto.getUserPhone());
        }
        if (userDto.getAuthority() != null){
            existedUser.setAuthority(userDto.getAuthority());
        }
        User updatedUser = userRepository.save(existedUser);
        return UserMapper.UMP.modelToDto(updatedUser);
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



    @Override
    public User  loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository
                .findByUserEmail (username)
                .orElseThrow(() -> new  UsernameNotFoundException("Aucun utilisateur ne corespond à cet identifiant"));
    }





   /* public void inscription(Utilisateur utilisateur) {

        if(!utilisateur.getEmail().contains("@")) {
            throw  new RuntimeException("Votre mail invalide");
        }
        if(!utilisateur.getEmail().contains(".")) {
            throw  new RuntimeException("Votre mail invalide");
        }

        Optional<Utilisateur> utilisateurOptional = this.utilisateurRepository.findByEmail(utilisateur.getEmail());
        if(utilisateurOptional.isPresent()) {
            throw  new RuntimeException("Votre mail est déjà utilisé");
        }
        String mdpCrypte = this.passwordEncoder.encode(utilisateur.getMdp());
        utilisateur.setMdp(mdpCrypte);

        Role roleUtilisateur = new Role();
        roleUtilisateur.setLibelle(TypeDeRole.UTILISATEUR);
        utilisateur.setRole(roleUtilisateur);

        utilisateur = this.utilisateurRepository.save(utilisateur);
        this.validationService.enregistrer(utilisateur);
    }

    public void activation(Map<String, String> activation) {
        Validation validation = this.validationService.lireEnFonctionDuCode(activation.get("code"));
        if(Instant.now().isAfter(validation.getExpiration())){
            throw  new RuntimeException("Votre code a expiré");
        }
        Utilisateur utilisateurActiver = this.utilisateurRepository.findById(validation.getUtilisateur().getId()).orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));
        utilisateurActiver.setActif(true);
        this.utilisateurRepository.save(utilisateurActiver);
    }
*/
}
