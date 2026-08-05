package sn.smartwaste.collect.waste.domain.model;

/**
 * Issue d'un passage d'agent sur un point de collecte (G1 du backlog).
 *
 * <p>La distinction est le cœur du lot : un obstacle n'est pas une collecte. Les confondre
 * fausserait tout taux de réalisation — et ferait disparaître de la tournée du lendemain un point
 * qu'on n'a justement pas pu vider.
 */
public enum PassageOutcome {
    /** Le point a été vidé. */
    COLLECTED,
    /** Le point n'a pas pu être desservi : voie barrée, bac absent, accès impossible. */
    INACCESSIBLE
}
