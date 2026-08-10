package sn.smartwaste.collect.waste.application.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.model.ImportedFeature;
import sn.smartwaste.collect.territory.application.api.TerritoryImportPort;
import sn.smartwaste.collect.waste.application.api.WasteImportPort;
import sn.smartwaste.collect.waste.domain.model.CircuitBalayageEntity;
import sn.smartwaste.collect.waste.domain.model.CircuitCollectEntity;
import sn.smartwaste.collect.waste.domain.model.CircuitShift;
import sn.smartwaste.collect.waste.domain.model.DepotoirEntity;
import sn.smartwaste.collect.waste.domain.model.TypeDepotoirEntity;
import sn.smartwaste.collect.waste.domain.repository.CircuitBalayageRepository;
import sn.smartwaste.collect.waste.domain.repository.CircuitCollectRepository;
import sn.smartwaste.collect.waste.domain.repository.DepotoirRepository;
import sn.smartwaste.collect.waste.domain.repository.TypeDepotoirRepository;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

/**
 * Traduit les entités brutes d'un fichier GeoJSON en entités du cœur métier déchets.
 *
 * <p>Le savoir déplacé ici est celui qui n'appartenait pas à l'import : que {@code Type_de_Mo}
 * désigne un type de point de collecte, que {@code shift} est une plage de balayage, qu'un type
 * inconnu se crée à la volée. Seul ce contexte peut en décider.
 *
 * <p><b>La géométrie est posée ici, mais construite ailleurs</b> (ADR-0016). Ce commentaire disait
 * l'inverse — « elle n'est pas reconstruite ici, elle appartient au référentiel territorial » — et
 * la conséquence n'est apparue qu'au premier import réel : dépotoirs 0/71, circuits de collecte
 * 0/52, circuits de balayage 0/156 sans la moindre coordonnée, quand communes et quartiers étaient
 * à 12/12 et 357/357. La carte ne renvoyait rien et aucune tournée ne pouvait être ordonnée
 * géographiquement. La construction reste au référentiel territorial, à qui ce modèle appartient
 * ({@code TerritoryImportPort.newGeometry}) ; ce contexte se contente de l'attacher à ses entités.
 */
@Service
@Transactional
public class WasteImportAdapter implements WasteImportPort {

    private static final String SYSTEM = "system";

    private final CircuitCollectRepository circuitCollectRepository;
    private final CircuitBalayageRepository circuitBalayageRepository;
    private final DepotoirRepository depotoirRepository;
    private final TypeDepotoirRepository typeDepotoirRepository;
    /** ADR-0016 : la construction de géométrie appartient au référentiel territorial. */
    private final TerritoryImportPort territory;

    public WasteImportAdapter(CircuitCollectRepository circuitCollectRepository,
                              CircuitBalayageRepository circuitBalayageRepository,
                              DepotoirRepository depotoirRepository,
                              TypeDepotoirRepository typeDepotoirRepository,
                              TerritoryImportPort territory) {
        this.circuitCollectRepository = circuitCollectRepository;
        this.circuitBalayageRepository = circuitBalayageRepository;
        this.depotoirRepository = depotoirRepository;
        this.typeDepotoirRepository = typeDepotoirRepository;
        this.territory = territory;
    }

    @Override
    public void importCircuitCollect(ImportedFeature feature, UUID communeId) {
        var circuit = new CircuitCollectEntity();
        circuit.setCode(feature.text("Code"));
        circuit.setName(feature.text("nom"));
        circuit.setLength(feature.text("Shape_Leng"));
        circuit.setFrequence(feature.text("Frequence"));
        circuit.setLatiPointA(feature.text("LatipointA"));
        circuit.setLatiPointD(feature.text("LatipointD"));
        circuit.setLongPointA(feature.text("LongpointA"));
        circuit.setLongPointD(feature.text("LongpointD"));
        circuit.setType(feature.text("type_id"));
        circuit.setCat(feature.text("cat_id"));
        circuit.setRotation(feature.text("Rotation"));
        circuit.setSection(feature.text("Section_id"));
        circuit.setSectection(feature.text("Sectection"));
        // ADR-0012 : référence par identifiant vers le référentiel territorial.
        circuit.setCommuneId(communeId);
        // ADR-0020 : import système, sans utilisateur authentifié à interroger — les fichiers
        // sources (datas/) ne décrivent que le territoire de Pikine.
        circuit.setOrganizationId(CurrentTenantProvider.PIKINE_ORGANIZATION_ID);
        stamp(circuit::setCreatedBy, circuit::setLastModifiedBy,
              circuit::setCreatedDate, circuit::setLastModifiedDate, circuit::setArchived);
        circuit.setGeometry(territory.newGeometry(feature));
        circuitCollectRepository.saveAndFlush(circuit);
    }

    @Override
    public void importCircuitBalayage(ImportedFeature feature, UUID communeId) {
        var circuit = new CircuitBalayageEntity();
        circuit.setCode(feature.text("code"));
        circuit.setName(feature.text("nomcircuit"));
        circuit.setShift(parseShift(feature.text("shift")));
        circuit.setLength(feature.text("longueur"));
        circuit.setCommuneId(communeId);
        circuit.setOrganizationId(CurrentTenantProvider.PIKINE_ORGANIZATION_ID);
        stamp(circuit::setCreatedBy, circuit::setLastModifiedBy,
              circuit::setCreatedDate, circuit::setLastModifiedDate, circuit::setArchived);
        circuit.setGeometry(territory.newGeometry(feature));
        circuitBalayageRepository.saveAndFlush(circuit);
    }

    @Override
    public void importDepotoir(ImportedFeature feature, UUID communeId) {
        var depotoir = new DepotoirEntity();
        depotoir.setAddress(feature.text("Adresse_de"));
        depotoir.setCommuneId(communeId);
        depotoir.setOrganizationId(CurrentTenantProvider.PIKINE_ORGANIZATION_ID);
        depotoir.setTypeDepotoir(resolveOrCreateType(feature.text("Type_de_Mo")));
        stamp(depotoir::setCreatedBy, depotoir::setLastModifiedBy,
              depotoir::setCreatedDate, depotoir::setLastModifiedDate, depotoir::setArchived);
        // ADR-0016 : un point de collecte sans position n'est pas supervisable — c'est la donnée
        // qui fait de lui un point. Son absence rendait la carte vide et interdisait toute
        // tournée ordonnée géographiquement.
        depotoir.setGeometry(territory.newGeometry(feature));
        depotoirRepository.saveAndFlush(depotoir);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCircuitCollects() {
        return circuitCollectRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCircuitBalayages() {
        return circuitBalayageRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countDepotoirs() {
        return depotoirRepository.count();
    }

    /**
     * Un libellé de type inconnu est créé à la volée : les fichiers source font autorité sur la
     * nomenclature, et refuser l'import parce qu'un type manque au référentiel serait absurde.
     */
    private TypeDepotoirEntity resolveOrCreateType(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        var type = typeDepotoirRepository.findByNameIgnoreCase(name);
        if (type == null) {
            type = new TypeDepotoirEntity();
            type.setName(name);
            stamp(type::setCreatedBy, type::setLastModifiedBy,
                  type::setCreatedDate, type::setLastModifiedDate, type::setArchived);
            // Le type doit être PERSISTÉ, pas seulement instancié : `Depotoir → TypeDepotoir` ne
            // cascade pas en PERSIST (P1-2 l'a réduit à REFRESH/MERGE pour qu'un dépôt ne puisse
            // pas écraser une valeur d'un référentiel partagé). Sans cet enregistrement, le
            // `saveAndFlush` du dépôt lève TransientObjectException.
            type = typeDepotoirRepository.saveAndFlush(type);
        }
        return type;
    }

    /** Une plage inconnue ne doit pas faire échouer tout le fichier. */
    private CircuitShift parseShift(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return CircuitShift.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void stamp(java.util.function.Consumer<String> createdBy,
                       java.util.function.Consumer<String> modifiedBy,
                       java.util.function.Consumer<LocalDateTime> createdDate,
                       java.util.function.Consumer<LocalDateTime> modifiedDate,
                       java.util.function.Consumer<Boolean> archived) {
        LocalDateTime now = LocalDateTime.now();
        createdBy.accept(SYSTEM);
        modifiedBy.accept(SYSTEM);
        createdDate.accept(now);
        modifiedDate.accept(now);
        archived.accept(false);
    }
}
