package sn.smartwaste.collect.waste.application.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.smartwaste.collect.shared.domain.model.ImportedFeature;
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

/**
 * Traduit les entités brutes d'un fichier GeoJSON en entités du cœur métier déchets.
 *
 * <p>Le savoir déplacé ici est celui qui n'appartenait pas à l'import : que {@code Type_de_Mo}
 * désigne un type de point de collecte, que {@code shift} est une plage de balayage, qu'un type
 * inconnu se crée à la volée. Seul ce contexte peut en décider.
 *
 * <p>La géométrie des circuits et des points de collecte n'est <b>pas</b> reconstruite ici : elle
 * appartient au référentiel territorial, et l'import ne la posait déjà pas sur ces entités.
 */
@Service
@Transactional
public class WasteImportAdapter implements WasteImportPort {

    private static final String SYSTEM = "system";

    private final CircuitCollectRepository circuitCollectRepository;
    private final CircuitBalayageRepository circuitBalayageRepository;
    private final DepotoirRepository depotoirRepository;
    private final TypeDepotoirRepository typeDepotoirRepository;

    public WasteImportAdapter(CircuitCollectRepository circuitCollectRepository,
                              CircuitBalayageRepository circuitBalayageRepository,
                              DepotoirRepository depotoirRepository,
                              TypeDepotoirRepository typeDepotoirRepository) {
        this.circuitCollectRepository = circuitCollectRepository;
        this.circuitBalayageRepository = circuitBalayageRepository;
        this.depotoirRepository = depotoirRepository;
        this.typeDepotoirRepository = typeDepotoirRepository;
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
        stamp(circuit::setCreatedBy, circuit::setLastModifiedBy,
              circuit::setCreatedDate, circuit::setLastModifiedDate, circuit::setArchived);
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
        stamp(circuit::setCreatedBy, circuit::setLastModifiedBy,
              circuit::setCreatedDate, circuit::setLastModifiedDate, circuit::setArchived);
        circuitBalayageRepository.saveAndFlush(circuit);
    }

    @Override
    public void importDepotoir(ImportedFeature feature, UUID communeId) {
        var depotoir = new DepotoirEntity();
        depotoir.setAddress(feature.text("Adresse_de"));
        depotoir.setCommuneId(communeId);
        depotoir.setTypeDepotoir(resolveOrCreateType(feature.text("Type_de_Mo")));
        stamp(depotoir::setCreatedBy, depotoir::setLastModifiedBy,
              depotoir::setCreatedDate, depotoir::setLastModifiedDate, depotoir::setArchived);
        depotoirRepository.saveAndFlush(depotoir);
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
