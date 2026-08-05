/**
 * Interface nommée <b>{@code api}</b> du module « Cœur métier Déchets ».
 *
 * <p>Seul point d'entrée légal des autres contextes. Entités, repositories, services internes et
 * contrôleurs restent <i>internes</i> au sens de Spring Modulith.
 *
 * <p><b>Pourquoi des projections et non les repositories.</b> « Supervision &amp; Analytique »
 * lisait directement {@code AlertRepository}, {@code DepotoirRepository} et les deux repositories
 * de circuits, puis naviguait dans les entités JPA — ce que l'ADR-0013 §3 interdit. Les contrats
 * publiés ici ne rendent que des <b>enregistrements autonomes</b> : aucun autre module ne voit une
 * entité, et le jour où le contexte devient un microservice, seule l'implémentation change.
 *
 * <p>Effet de bord utile : les projections sont produites <b>dans la transaction du contexte
 * propriétaire</b>. L'accès à {@code typeDepotoir}, qui est LAZY depuis P1-2, ne dépend donc plus
 * de la transaction de l'appelant.
 */
@org.springframework.modulith.NamedInterface("api")
package sn.smartwaste.collect.waste.application.api;
