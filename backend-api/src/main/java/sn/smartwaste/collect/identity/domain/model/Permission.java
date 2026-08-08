package sn.smartwaste.collect.identity.domain.model;

import java.text.MessageFormat;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Permission {

    // =====================================================================================
    // Cinq valeurs heritees d'un AUTRE produit (com.worldline.tapandgo), conservees : chacune a
    // des lignes reelles dans authorityPermission (seed Liquibase, changelogs 1.0.0/2.1.0/2.13.0)
    // et devient un vrai SimpleGrantedAuthority a chaque authentification
    // (UserEntity.getAuthorities()). Les retirer casserait la connexion des comptes qui les
    // portent (SUPER_ADMIN, ADMIN, USER, AGENT) sans une migration de purge dediee — verifie le
    // 2026-08-08 par recherche exhaustive de chaque valeur dans tous les changelogs/seeds.
    // ACCESS_MY_ACTIVITIES est le nom emprunte que porte l'agent de collecte, qui ne dit pas ce
    // qu'il a le droit de faire — a renommer un jour, mais pas a supprimer.
    // =====================================================================================
    USER_VIEW ("USER_VIEW"),
    ACCESS_ADMIN ("ACCESS_ADMIN"),
    MANAGE_ROLE ("MANAGE_ROLE"),
    CREATE_USER ( "CREATE_USER"),
    ACCESS_MY_ACTIVITIES ("ACCESS_MY_ACTIVITIES"),

    // =====================================================================================
    // Permissions du domaine « gestion des dechets ». Celles-ci nomment les actes reels du
    // metier. Les ~30 autres valeurs heritees de com.worldline.tapandgo (ACCESS_MY_USER,
    // DISTRIBUTE_PRODUCT, VALIDATE_PAYMENT_MEAN, ACCESS_TERMINAL_INFO…) ont ete retirees le
    // 2026-08-08 (validation explicite obtenue) : aucun lecteur en code, aucune ligne en base
    // dans aucun changelog — contrairement au bloc ci-dessus.
    // =====================================================================================

    /** Consulter la tournee d'une commune. */
    VIEW_COLLECTION_ROUTE ("VIEW_COLLECTION_ROUTE"),
    /** Declarer un point collecte ou inaccessible. */
    DECLARE_COLLECTION ("DECLARE_COLLECTION"),
    /** Lire les indicateurs, rapports et journaux de supervision. */
    VIEW_SUPERVISION ("VIEW_SUPERVISION"),
    /** Enroler et revoquer capteurs et traceurs. */
    MANAGE_DEVICES ("MANAGE_DEVICES"),
    /** Rediger et programmer un message de sensibilisation. */
    SEND_AWARENESS ("SEND_AWARENESS");


    private String value;

    Permission(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Permission fromValue(String value) {
        for (Permission permission : values()) {
            if (permission.value.equalsIgnoreCase(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException(MessageFormat.format("{0} not found with the value: {1}", Permission.class.getSimpleName(), value));
    }

}
