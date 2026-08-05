package sn.smartwaste.collect.platform.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Cycle de vie d'un signalement citoyen.
 *
 * <p>Le champ était auparavant une <b>chaîne libre</b> initialisée à « EN_ATTENTE » et que rien ne
 * faisait jamais évoluer : un habitant pouvait signaler un dépôt sauvage, mais personne ne pouvait
 * le traiter, ni savoir ce qui l'avait été. Un signalement qu'on ne peut pas clore n'est pas un
 * signalement, c'est une boîte aux lettres.
 *
 * <p>Les transitions sont explicites : elles empêchent qu'un signalement déjà clos soit rouvert par
 * inadvertance, ou qu'il saute l'étape de prise en charge.
 */
public enum AvisStatus {

    /** Déposé par l'habitant, pas encore pris en charge. */
    SIGNALE,

    /** Pris en charge par un agent ou un superviseur. */
    EN_COURS,

    /** Intervention faite. État terminal. */
    TRAITE,

    /** Écarté (doublon, hors périmètre, non fondé). État terminal. */
    REJETE;

    /** États atteignables depuis celui-ci. Vide pour les états terminaux. */
    public Set<AvisStatus> allowedNext() {
        return switch (this) {
            case SIGNALE -> EnumSet.of(EN_COURS, REJETE);
            // On peut encore rejeter après prise en charge : c'est souvent en se déplaçant
            // qu'on découvre qu'un signalement est infondé.
            case EN_COURS -> EnumSet.of(TRAITE, REJETE);
            case TRAITE, REJETE -> EnumSet.noneOf(AvisStatus.class);
        };
    }

    public boolean canTransitionTo(AvisStatus target) {
        return allowedNext().contains(target);
    }

    public boolean isTerminal() {
        return allowedNext().isEmpty();
    }
}
