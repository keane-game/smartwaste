package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.model.AuthorityEntity;
import sonaged.collecte.master.model.UserEntity;
import sonaged.collecte.master.repository.AuthorityRepository;
import sonaged.collecte.master.repository.UserRepository;
import sonaged.collecte.master.dto.User;
import sonaged.collecte.master.mapper.UserMapper;
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

    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;


    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User readUser(Long userId) {
        var user  = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(userId)
                ));
        return UserMapper.UMP.asDto(user);
    }


    @Override
    public List<User> readAllUser() {
        var userList = userRepository.findAll();
        return UserMapper.UMP.asListDto(userList);
    }


    @Override
    public User createUser(User user) {
        user.setUserPassword(bCryptPasswordEncoder.encode("Sonaged@123"));
        var userSave = userRepository.save(UserMapper.UMP.asModel(user));
        return UserMapper.UMP.asDto(userSave);
    }


    @Override
    public User updateUser(Long userId, User userResponse) {
        var existedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "User with id [%s] not found to update ".formatted(userId)
        ));
        if (!Objects.equals(existedUser.getUserId(), userResponse.getUserId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        if (userResponse.getUserEmail() != null){
            existedUser.setUserEmail(userResponse.getUserEmail());
        }
        if (userResponse.getUserFirstname () != null){
            existedUser.setUserFirstname (userResponse.getUserFirstname ());
        }

        if (userResponse.getUserLastname () != null){
            existedUser.setUserLastname (userResponse.getUserLastname ());
        }

        if (userResponse.getUserCode() != null){
            existedUser.setUserCode(userResponse.getUserCode());
        }
        if (userResponse.getUserPhone() != null){
            existedUser.setUserPhone(userResponse.getUserPhone());
        }
        if (userResponse.getAuthority() != null){
            AuthorityEntity authority = authorityRepository.findById (userResponse.getAuthority().getAuthorityId ()).get();
            existedUser.setAuthority(authority);
        }
        var updatedUser = userRepository.save(existedUser);
        return UserMapper.UMP.asDto(updatedUser);
    }


    @Override
    public void deleteUser(Long userId) {
        var user  = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found to delete".formatted(userId)
                ));
        userRepository.delete(user);
    }



    @Override
    public UserEntity loadUserByUsername(String username) throws ResourceNotFoundException {
        return this.userRepository
                .findByUserEmail (username)
                .orElseThrow(() -> new  ResourceNotFoundException("Email ou mot de passe incorrect!"));
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
