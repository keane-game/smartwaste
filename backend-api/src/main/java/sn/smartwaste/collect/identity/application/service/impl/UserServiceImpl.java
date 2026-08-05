package sn.smartwaste.collect.identity.application.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.identity.application.mapper.UserMapper;
import sn.smartwaste.collect.identity.application.service.UserService;

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

    // Injecté par constructeur (via @RequiredArgsConstructor) et non plus par champ @Autowired :
    // une dépendance posée par réflexion après construction ne peut pas être fournie par un test
    // sans démarrer un contexte Spring — l'encodage du mot de passe restait donc non testable.
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User readUser(UUID userId) {
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
    public Page<User> readAllUser(Pageable pageable) {
        return userRepository.findAll (pageable).map (UserMapper.UMP::asDto);

    }

    /**
     * Création d'un compte par un administrateur.
     *
     * <p><b>Le mot de passe n'est plus une constante.</b> L'ancien code encodait
     * {@code "Sonaged@123"} pour <i>tous</i> les comptes créés ici : la valeur est en clair dans le
     * dépôt et dans son historique, donc connaître l'adresse d'un collègue suffisait à entrer dans
     * son compte. `AuthServiceImpl.register` avait déjà été corrigé ; ce chemin-là, réservé à
     * l'administration, était resté en arrière — et il attribue en plus le rôle, ce qui en faisait
     * le plus intéressant des deux à emprunter.
     *
     * <p>Le mot de passe fourni est exigé et haché, exactement comme à l'inscription.
     */
    @Override
    public User createUser(User user) {
        if (user.getUserPassword() == null || user.getUserPassword().isBlank()) {
            throw new ResourceNotFoundException("Le mot de passe est obligatoire");
        }
        user.setUserPassword(bCryptPasswordEncoder.encode(user.getUserPassword()));
        var userSave = userRepository.save(UserMapper.UMP.asModel(user));
        return UserMapper.UMP.asDto(userSave);
    }

    @Override
    public User updateUser(UUID userId, User user) {
        var existedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "User with id [%s] not found to update ".formatted(userId)
        ));

        if (user.getUserEmail() != null){
            existedUser.setUserEmail(user.getUserEmail());
        }
        if (user.getUserFirstname () != null){
            existedUser.setUserFirstname (user.getUserFirstname ());
        }

        if (user.getUserLastname () != null){
            existedUser.setUserLastname (user.getUserLastname ());
        }

        if (user.getUserCode() != null){
            existedUser.setUserCode(user.getUserCode());
        }
        if (user.getUserPhone() != null){
            existedUser.setUserPhone(user.getUserPhone());
        }
        if (user.getUserAddress () != null){
            existedUser.setUserAddress (user.getUserAddress ());
        }
        if (user.getAuthority() != null){
            AuthorityEntity authority = authorityRepository.findById (user.getAuthority().getAuthorityId ()).get();
            existedUser.setAuthority(authority);
        }
        var updatedUser = userRepository.save(existedUser);
        return UserMapper.UMP.asDto(updatedUser);
    }

    @Override
    public void deleteUser(UUID userId) {
        var user  = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found to delete".formatted(userId)
                ));
        userRepository.delete(user);
    }

    /**
     * Charge le compte <b>et ses permissions</b> pour l'authentification.
     *
     * <p><b>Pourquoi la transaction est indispensable ici.</b> {@code AuthorityEntity.permissions}
     * est en {@code LAZY}, et {@code JwtFilter} appelle cette méthode puis
     * {@code getAuthorities()} — or un filtre s'exécute <b>avant</b> l'open-session-in-view de
     * Spring. Sans transaction, la session se referme entre les deux et les permissions
     * disparaissent en silence : le compte serait authentifié avec son seul rôle, et toute règle
     * fondée sur une permission refuserait l'accès sans que rien ne l'explique.
     *
     * <p>La collection est donc initialisée explicitement, tant que la session est ouverte. Elle
     * compte au plus quelques valeurs par rôle : le coût est celui d'une jointure, pas d'un N+1.
     */
    @Override
    @Transactional(readOnly = true)
    public UserEntity loadUserByUsername(String username) throws ResourceNotFoundException {
        UserEntity user = this.userRepository
                .findByUserEmail (username)
                .orElseThrow(() -> new  ResourceNotFoundException("Email ou mot de passe incorrect!"));
        if (user.getAuthority() != null && user.getAuthority().getPermissions() != null) {
            user.getAuthority().getPermissions().size();
        }
        return user;
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
