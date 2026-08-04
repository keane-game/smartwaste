package sn.smartwaste.collect.waste.application.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import sn.smartwaste.collect.identity.application.api.CurrentUserProvider;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.service.CollectionPassageService;
import sn.smartwaste.collect.waste.domain.model.AlertEntity;
import sn.smartwaste.collect.waste.domain.model.CollectionPassage;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.model.PassageOutcome;
import sn.smartwaste.collect.waste.domain.repository.AlertRepository;
import sn.smartwaste.collect.waste.domain.repository.CollectionPassageRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;

/**
 * Enregistre ce qu'un agent a fait sur un point de collecte (G1 du backlog).
 *
 * <p><b>Le maillon qui manquait.</b> La boucle métier est « détecter → alerter → collecter →
 * constater ». Les trois premiers temps fonctionnaient depuis le 2026-07-30 ; le quatrième
 * n'existait pas. Conséquences en chaîne : le niveau ne retombait que si un capteur le disait — or
 * 71 points sur 71 n'en ont pas —, l'alerte restait ouverte indéfiniment, la tournée reproposait le
 * point, et aucun indicateur d'efficacité n'était calculable.
 *
 * <h2>Deux décisions, explicites parce qu'elles se contrediraient en silence</h2>
 *
 * <p><b>1. Un passage vaut information sur l'état, au même titre qu'une mesure.</b> Il rafraîchit
 * donc {@code lastMeasuredAt}. Sans cela, un point vidé le matin retomberait en
 * {@code ETAT_INCONNU} après 24 h et remonterait en tête de tournée : les agents reverraient chaque
 * jour les points qu'ils viennent de vider. <b>Le capteur garde le dernier mot</b> — une mesure
 * postérieure écrase la déclaration, {@code FillLevelProjector} ignorant déjà toute mesure
 * antérieure au dernier état connu. L'agent dit ce qu'il a fait, le capteur dit ce qui est.
 *
 * <p><b>2. Une alerte de maintenance survit à la collecte.</b> Vider un bac ne répare pas le
 * capteur qui l'observe. Refermer cette alerte-là ferait disparaître un problème non résolu et
 * laisserait le point en angle mort silencieux.
 */
@Service
public class CollectionPassageServiceImpl implements CollectionPassageService {

    private static final Logger log = LoggerFactory.getLogger(CollectionPassageServiceImpl.class);

    private final DepotoirRepository depotoirRepository;
    private final AlertRepository alertRepository;
    private final CollectionPassageRepository passageRepository;
    private final CurrentUserProvider currentUserProvider;
    /** Pour nommer l'agent dans l'alerte refermee : un UUID ne renseigne personne. */
    private final sn.smartwaste.collect.identity.application.api.UserDirectory userDirectory;
    private final TerritorialAccessGuard accessGuard;
    private final Clock clock;

    public CollectionPassageServiceImpl(DepotoirRepository depotoirRepository,
                                        AlertRepository alertRepository,
                                        CollectionPassageRepository passageRepository,
                                        CurrentUserProvider currentUserProvider,
                                        sn.smartwaste.collect.identity.application.api.UserDirectory userDirectory,
                                        TerritorialAccessGuard accessGuard,
                                        Clock clock) {
        this.depotoirRepository = depotoirRepository;
        this.alertRepository = alertRepository;
        this.passageRepository = passageRepository;
        this.currentUserProvider = currentUserProvider;
        this.userDirectory = userDirectory;
        this.accessGuard = accessGuard;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void markCollected(Long depotoirId) {
        DepotoirEntity depotoir = require(depotoirId);
        requireTerritorialAccess(depotoir);
        Instant now = Instant.now(clock);

        depotoir.setFillLevelPercent(0);
        depotoir.setLastCollectedAt(now);
        // Cf. décision 1 : un passage est une information sur l'état, pas seulement un acte.
        depotoir.setLastMeasuredAt(now);
        depotoirRepository.save(depotoir);

        int refermees = resolveCollectionAlerts(depotoir, now);
        record(depotoirId, PassageOutcome.COLLECTED, null, now);

        log.info("Point {} collecte — {} alerte(s) refermee(s)", depotoirId, refermees);
    }

    @Override
    @Transactional
    public void markInaccessible(Long depotoirId, String reason) {
        requireTerritorialAccess(require(depotoirId));
        Instant now = Instant.now(clock);
        // Rien n'est vidé, rien n'est refermé : un obstacle n'est pas une collecte. Le point
        // reparaîtra dans la tournée du lendemain, ce qui est exactement l'intention.
        record(depotoirId, PassageOutcome.INACCESSIBLE, reason, now);
        log.info("Point {} inaccessible : {}", depotoirId, reason);
    }

    /**
     * Referme les alertes de collecte du point — et <b>seulement</b> celles-là.
     *
     * @return le nombre d'alertes refermées
     */
    private int resolveCollectionAlerts(DepotoirEntity depotoir, Instant now) {
        int refermees = 0;
        for (AlertEntity alert : alertRepository.findByDepotoirIdAndResolvedAtIsNull(
                depotoir.getDepotoirId())) {
            if (SensorSilenceProjector.OBJET_SILENCE.equals(alert.getObject())) {
                // Cf. décision 2 : vider un bac ne répare pas le capteur.
                continue;
            }
            alert.setResolvedAt(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
            alert.setResolvedBy(agentLabel());
            alertRepository.save(alert);
            refermees++;
        }
        return refermees;
    }

    /**
     * Nom lisible de l'agent, pour l'alerte refermée.
     *
     * <p>Stockait l'{@code UUID} : le journal d'un point affichait
     * « ALERTE_RESOLUE … 019fb3cc-75dc-7ef1-… », que personne ne peut lire. Même travers que
     * « commune 019fb3cc… » dans les rapports. Un annuaire muet — compte supprimé — ne doit pas
     * empêcher la collecte d'aboutir : on retombe sur un libellé générique.
     */
    private String agentLabel() {
        UUID agent = currentUser();
        if (agent == null) {
            return "agent";
        }
        return userDirectory.emailOf(agent).orElseGet(agent::toString);
    }

    private void record(Long depotoirId, PassageOutcome outcome, String reason, Instant now) {
        var passage = new CollectionPassage();
        passage.setDepotoirId(depotoirId);
        passage.setAgentId(currentUser());
        passage.setOutcome(outcome);
        passage.setReason(reason);
        passage.setOccurredAt(now);
        passageRepository.save(passage);
    }

    /**
     * Un agent n'intervient que sur les communes qui lui sont affectees.
     *
     * <p><b>Cette verification a d'abord manque.</b> L'affectation territoriale a ete creee avec ce
     * lot, puis laissee inerte : l'autorisation s'arretait au role, si bien que n'importe quel
     * agent pouvait declarer collecte n'importe lequel des 71 points. Or une remise a zero du
     * niveau referme l'alerte et sort le point de la tournee — de quoi faire disparaitre un
     * debordement reel depuis un compte etranger au terrain concerne. C'est le travers que ce
     * depot connait deja : des regles d'autorisation declarees que rien n'applique.
     *
     * <p>L'administration n'est pas bornee : elle supervise les 12 communes, et lui imposer une
     * affectation la bloquerait sur son propre outil.
     *
     * <p>Un point <b>sans commune</b> — 15 des 71 importes, faute de libelles concordants — n'est
     * couvert par personne : il reste reserve a l'administration. L'ouvrir a tous rouvrirait le
     * trou par la porte de derriere.
     */
    private void requireTerritorialAccess(DepotoirEntity depotoir) {
        accessGuard.requireAccessTo(depotoir.getCommuneId());
    }

    private DepotoirEntity require(Long depotoirId) {
        return depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Point de collecte inconnu : " + depotoirId));
    }

    /** L'agent courant, {@code null} si l'appel ne vient pas d'un compte (tâche planifiée). */
    private UUID currentUser() {
        try {
            return currentUserProvider.requireCurrentUserId();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
