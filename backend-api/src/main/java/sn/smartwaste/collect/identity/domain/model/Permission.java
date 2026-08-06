package sn.smartwaste.collect.identity.domain.model;

import java.text.MessageFormat;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Permission {


   USER_VIEW ("USER_VIEW"),
    ACCESS_ADMIN ("ACCESS_ADMIN"),
    MANAGE_ROLE ("MANAGE_ROLE"),
    CREATE_USER ( "CREATE_USER"),
    ACCESS_MY_USER ("ACCESS_MY_USER"),
    ACCESS_ALL_USERS ("ACCESS_ALL_USERS"),
    ACCESS_CONTROLS ("ACCESS_CONTROLS"),
    ACCESS_MY_ACCOUNT ("ACCESS_MY_ACCOUNT"),
    ACCESS_MY_ACTIVITIES ("ACCESS_MY_ACTIVITIES"),
    ACCESS_MY_ADDRESS ("ACCESS_MY_ADDRESS"),
    ACCESS_MY_CONSENT ("ACCESS_MY_CONSENT"),
    ACCESS_MY_CONTRACTS ("ACCESS_MY_CONTRACTS"),
    ACCESS_MY_SUBSCRIPTIONS ("ACCESS_MY_SUBSCRIPTIONS"),

    ACCESS_PRODUCT ("ACCESS_PRODUCT"),
    ACCESS_TERMINAL_INFO ("ACCESS_TERMINAL_INFO"),
    DISTRIBUTE_PRODUCT ("DISTRIBUTE_PRODUCT"),
    LOGIN_CONTROLER ("LOGIN_CONTROLER"),
    VALIDATE_ADDRESSES ("VALIDATE_ADDRESSES"),
    VALIDATE_IDENTITY ("VALIDATE_IDENTITY"),
    VALIDATE_PAYMENT_MEAN ("VALIDATE_PAYMENT_MEAN"),
    ACCESS_STATISTICS ("ACCESS_STATISTICS"),
    ACCESS_TOPOLOGY ("ACCESS_TOPOLOGY"),
    MANAGE_VALIDATION_REQUEST ("MANAGE_VALIDATION_REQUEST"),
    ACCESS_ALL_EVENTS ("ACCESS_ALL_EVENTS"),
    ACCESS_MY_EVENTS ("ACCESS_MY_EVENTS"),
    CONFIGURE_MY_USER("CONFIGURE_MY_USER"),
    MANAGE_ALARM("MANAGE_ALARM"),
    MANAGE_STATISTICS("MANAGE_STATISTICS"),
    REMOVE_ACCOUNT("REMOVE_ACCOUNT"),
    ACCESS_MONITORING_MYCOMPANY_TERMINAL("ACCESS_MONITORING_MYCOMPANY_TERMINAL"),
    ACCESS_MYCOMPANY_STATISTICS("ACCESS_MYCOMPANY_STATISTICS"),
    ACCESS_MYCOMPANY_USERS("ACCESS_MYCOMPANY_USERS"),
    ACCESS_STATISTICS_MYCOMPANY_TERMINAL("ACCESS_STATISTICS_MYCOMPANY_TERMINAL"),
    MANAGE_ROLES ("MANAGE_ROLES"),
    ACCESS_RULES_PARAMETERS ("ACCESS_RULES_PARAMETERS"),

    // =====================================================================================
    // Permissions du domaine « gestion des dechets ».
    //
    // Toutes celles qui precedent viennent d'un AUTRE produit (com.worldline.tapandgo) :
    // DISTRIBUTE_PRODUCT, VALIDATE_PAYMENT_MEAN, ACCESS_TERMINAL_INFO… Elles ne decrivent rien
    // d'ici. L'agent de collecte portait ainsi ACCESS_MY_ACTIVITIES — un nom emprunte qui ne dit
    // pas ce qu'il a le droit de faire, et qu'aucun lecteur du code ne peut relier a une tournee.
    //
    // Celles-ci nomment les actes reels du metier. Les etrangeres ne sont PAS supprimees ici :
    // leur retrait est une suppression de code — validation requise (regle projet), independamment
    // du fait que plus rien ne les reference. AuthorityRules et UserRules (seuls lecteurs restants,
    // avec MANAGE_ROLE) ont ete retires le 2026-08-06 : MANAGE_ROLE reste utilisee reellement
    // (@PreAuthorize, AuthorityController) ; ACCESS_ADMIN et USER_VIEW n'ont plus aucun lecteur.
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
