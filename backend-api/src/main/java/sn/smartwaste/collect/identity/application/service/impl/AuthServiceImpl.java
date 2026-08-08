package sn.smartwaste.collect.identity.application.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.smartwaste.collect.identity.application.dto.Authentification;
import sn.smartwaste.collect.identity.application.dto.User;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.identity.application.mapper.UserMapper;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.model.Validation;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.identity.infrastructure.security.JwtService;
import sn.smartwaste.collect.identity.application.service.AuthService;
import sn.smartwaste.collect.identity.application.service.SessionService;
import sn.smartwaste.collect.identity.application.service.ValidationService;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * Rôle attribué d'office à tout compte créé par l'inscription publique.
     *
     * <p>Semé par le changelog {@code 2.1.0_identity_uuid.xml} — c'est le rôle le moins privilégié
     * des trois rôles de référence.
     */
    private static final String DEFAULT_REGISTRATION_ROLE = "USER";

    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserRepository userRepository;
    private AuthorityRepository authorityRepository;
    private SessionService sessionService;

    /**
     * Inscription d'un citoyen — <b>atomique</b> : compte, code d'activation et envoi du courriel
     * réussissent ensemble ou pas du tout.
     *
     * <p><b>Ce que {@code @Transactional} ferme ici.</b> La méthode enregistrait le compte, puis
     * publiait l'événement d'activation, écouté <i>synchronement</i> par l'envoi de courriel.
     * Serveur SMTP absent — son état par défaut, {@code smtp4dev} n'étant pas démarré —
     * l'exception remontait à l'appelant : <b>compte créé en base, 500 rendu au client</b>. Le
     * citoyen croyait avoir échoué, recommençait, et s'entendait répondre « Votre email est déjà
     * utilisé » : il ne pouvait ni entrer (compte non activé) ni se réinscrire, et aucun endpoint
     * ne renvoie le code. Il était bloqué sans recours.
     *
     * <p>Annuler vaut mieux qu'un demi-succès : un compte dont le code n'est jamais parvenu est une
     * adresse rendue inutilisable, alors qu'un état propre laisse la tentative suivante aboutir.
     *
     * <p>Effet de bord assumé : la méthode reposait jusqu'ici sur l'{@code open-session-in-view}
     * de la couche web pour résoudre les collections paresseuses du rôle. Hors requête HTTP —
     * appel direct, tâche planifiée, test — elle levait {@code LazyInitializationException}. Une
     * transaction explicite ne dépend plus de la façon dont on l'appelle.
     */
    @Transactional
    public void register(User user) {

        if(!user.getUserEmail().contains("@")) {
            throw  new ResourceNotFoundException("Votre email est invalide");
        }
        if(!user.getUserEmail().contains(".")) {
            throw  new ResourceNotFoundException("Votre email est invalide");
        }

        Optional<UserEntity> userOptional = this.userRepository.findByUserEmail(user.getUserEmail());
        if(userOptional.isPresent()) {
            throw  new ResourceNotFoundException ("Votre email est déjà utilisé");
        }
        if (user.getUserPassword() == null || user.getUserPassword().isBlank()) {
            throw new ResourceNotFoundException("Le mot de passe est obligatoire");
        }
        String pwdCrypt = this.passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(pwdCrypt);

        user.setActivated(false);

        UserEntity userToCreate = UserMapper.UMP.asModel(user);

        // ---------------------------------------------------------------------------------
        // Prise de contrôle de compte par collision d'identifiant refermée.
        //
        // `UserEntity.userId` est généré par l'application (`@UuidGenerator`), et `User` (DTO)
        // porte un champ `userId` public, jamais neutralisé jusqu'ici. `/auth/register` est en
        // `permitAll` : un appelant pouvait fournir dans le corps de la requête le `userId` d'un
        // compte EXISTANT. Spring Data voit alors un `@Id` non nul et fait un `merge` (UPDATE) au
        // lieu d'un `persist` (INSERT) — le compte visé se retrouvait avec l'email et le mot de
        // passe de l'attaquant, désactivé (`activated=false` ci-dessous), prêt à être réactivé par
        // l'attaquant via SON code d'activation. Même famille de faille que celle fermée sur
        // `Avis.create()` (`avis.setId(null)`), jamais reproduite ici alors que le module identité
        // est bien plus sensible. Remis à `null` pour garantir un INSERT, jamais un UPDATE
        // déguisé — l'identifiant appartient au serveur, pas au corps de la requête.
        userToCreate.setUserId(null);

        // ---------------------------------------------------------------------------------
        // Élévation de privilèges refermée.
        //
        // `/auth/register` est en `permitAll`, et l'ancien code faisait
        // `user.setAuthority(user.getAuthority())` : le rôle arrivait donc **du corps de la
        // requête**. N'importe qui pouvait s'inscrire avec l'identifiant du rôle SUPER_ADMIN,
        // recevoir le code d'activation à sa propre adresse, activer le compte, et se retrouver
        // administrateur. Le rôle est désormais imposé par le serveur et ce qui vient du client
        // est écrasé — c'est fait **après** le mapping DTO -> entité, pour qu'aucune valeur
        // fournie par l'appelant ne puisse survivre.
        //
        // Effet de bord attendu : cela répare aussi l'inscription, jusqu'ici cassée. Le
        // formulaire Angular n'envoie aucun `authority` alors que la colonne est `nullable=false`,
        // donc tout signup légitime échouait sur une violation de contrainte.
        // ---------------------------------------------------------------------------------
        userToCreate.setAuthority(defaultRegistrationAuthority());

        var userEntity = this.userRepository.save(userToCreate);
        // Dans la MEME transaction que la creation du compte (cf. javadoc de la methode) : si le
        // code d'activation ne part pas, le compte ne doit pas subsister.
        this.validationService.registerUserCode(userEntity);
    }

    private AuthorityEntity defaultRegistrationAuthority() {
        return this.authorityRepository
                .findByNameAndDeletionStatus(DEFAULT_REGISTRATION_ROLE, DeletionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException(
                        "Rôle par défaut « %s » introuvable : les rôles de référence ne sont pas semés"
                                .formatted(DEFAULT_REGISTRATION_ROLE)));
    }

    public void activation(String code) {
        Validation validation = this.validationService.readByCode(code);
        if(Instant.now().isAfter(validation.getExpiration())){
            throw  new ResourceNotFoundException("Votre code a expiré");
        }
        UserEntity userActive = this.userRepository.findById(validation.getUser ( ).getUserId ())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur inconnu"));
        userActive.setActivated (true);
        this.userRepository.save(userActive);
    }

    public Map<String, String> authentication(Authentification authentification) {
        log.info("connexion");
        final Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken (authentification.username(), authentification.password())
        );
        log.info("connexion");
        if(authenticate.isAuthenticated()) {
            UserEntity user = loadUserByUsername(authentification.username());
            var tokens = this.sessionService.openSession(user);
            // Forme de réponse conservée (`bearer`) pour ne pas casser les clients existants ;
            // `refresh` est purement additif.
            return Map.of("bearer", tokens.bearer(), "refresh", tokens.refresh());
        }
        return null;
    }

    public UserEntity loadUserByUsername(String username) throws ResourceNotFoundException {
        return this.userRepository
                .findByUserEmail (username)
                .orElseThrow(() -> new  ResourceNotFoundException("Email ou mot de passe incorrect!"));
    }



}
