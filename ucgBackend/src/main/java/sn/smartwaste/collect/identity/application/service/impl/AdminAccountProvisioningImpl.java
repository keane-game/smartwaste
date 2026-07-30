package sn.smartwaste.collect.identity.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import sn.smartwaste.collect.identity.application.api.AdminAccountProvisioning;
import sn.smartwaste.collect.identity.domain.model.UserEntity;
import sn.smartwaste.collect.identity.domain.repository.AuthorityRepository;
import sn.smartwaste.collect.identity.domain.repository.UserRepository;
import sn.smartwaste.collect.shared.domain.model.DeletionStatus;

/**
 * Crée le compte d'administration initial (ADR-0014 §2).
 *
 * <p><b>Le trou que cela ferme.</b> Après les migrations UUID, la table {@code users} est vide :
 * {@code 2.1.0-3} la purge avant de convertir la clé primaire, et le seed d'origine
 * ({@code data/user.sql}, identifiants {@code BIGINT}) n'a jamais été rejoué sous la nouvelle forme
 * — alors que les rôles, eux, l'ont été ({@code 2.1.0-7}). Aucun chemin ne menait donc à un compte
 * {@code ADMIN} : {@code /auth/register} impose {@code USER} et l'activation exige un code envoyé
 * par courriel. Toute la surface d'administration était inatteignable.
 *
 * <p><b>Pourquoi pas un changeset Liquibase.</b> Semer un compte par changelog supposerait de
 * committer l'empreinte bcrypt d'un mot de passe connu : un secret versionné de plus dans un dépôt
 * dont la rotation n'a jamais été faite (ADR-0002 §4-5), <b>irrévocable</b> puisqu'une somme de
 * contrôle appliquée est figée, et <b>partagé par tous les déploiements</b> issus du même dépôt.
 * C'est exactement le défaut que {@code UserServiceImpl.createUser} a mis des mois à perdre avec son
 * {@code Sonaged@123}. Ici le secret ne transite que par l'environnement de l'appelant.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAccountProvisioningImpl implements AdminAccountProvisioning {

    private static final String ROLE = "SUPER_ADMIN";

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Outcome createAdministratorIfAbsent(String email, String rawPassword) {
        if (userRepository.findByUserEmail(email).isPresent()) {
            return Outcome.ALREADY_PRESENT;
        }
        var authority = authorityRepository.findByNameAndDeletionStatus(ROLE, DeletionStatus.ACTIVE);
        if (authority.isEmpty()) {
            return Outcome.ROLE_MISSING;
        }

        var admin = new UserEntity();
        admin.setUserEmail(email);
        admin.setUserPassword(encoder.encode(rawPassword));
        admin.setUserFirstname("Administration");
        admin.setUserLastname("SONAGED");
        admin.setAuthority(authority.get());
        // Activé d'emblée : le circuit d'activation par courriel suppose un compte capable de
        // recevoir un code, ce qui n'a pas de sens pour le tout premier.
        admin.setActivated(true);
        admin.setCreatedBy("bootstrap");
        admin.setLastModifiedBy("bootstrap");

        userRepository.save(admin);
        return Outcome.CREATED;
    }
}
