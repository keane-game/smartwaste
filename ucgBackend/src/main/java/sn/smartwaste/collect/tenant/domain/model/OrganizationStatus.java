package sn.smartwaste.collect.tenant.domain.model;

/** Cycle de vie d'une collectivité cliente. */
public enum OrganizationStatus {
    /** Abonnement en cours : les utilisateurs rattachés peuvent travailler. */
    ACTIVE,
    /** Accès suspendu (impayé, litige) — les données sont conservées. */
    SUSPENDED
}
