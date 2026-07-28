package sn.smartwaste.collect.analytics.application.service.impl;

import sn.smartwaste.collect.analytics.application.dto.DepartmentState;
import sn.smartwaste.collect.analytics.application.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.territory.domain.repository.CommuneRepository;
import sn.smartwaste.collect.waste.application.api.WasteReadModel;

import java.util.List;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardServiceImpl implements DashboardService {

    final CommuneRepository communeRepository;

    /**
     * Port publié par le contexte « Déchets ». Remplace l'injection directe de six repositories
     * (ADR-0013 §3) — dont sept étaient d'ailleurs injectés sans jamais être utilisés :
     * `circuitRepository`, `typeDepotoirRepository`, `geometryRepository`, `coordinateRepository`,
     * `departmentRepository`, `regionRepository` et `quartierRepository`.
     */
    final WasteReadModel wasteReadModel;

    @Override
    public Long totalCommunes() {
        return communeRepository.count();
    }

    @Override
    public Long totalDepotoirs() {
        return wasteReadModel.countCollectionPoints();
    }

    @Override
    public Long totalBennes() {
        return wasteReadModel.countStreetFurniture();
    }

    @Override
    public Long totalCircuits() {
        return wasteReadModel.countCircuits();
    }

    @Override
    public Long totalBacs() {
        return wasteReadModel.countCollectionPointsByTypeNameContaining("Bac");
    }

    /*
        PNR: Point de regroupement normalisée
    */
    @Override
    public Long totalPRN() {
        return wasteReadModel.countCollectionPointsByTypeNameContaining("PRN");
    }

    /*
        PP: Point  Propres
    */
    @Override
    public Long totalPP() {

        return wasteReadModel.countCollectionPointsByTypeNameContaining("PP");
    }

    /*
     CP: Caisses  Polybennes
   */
    @Override
    public Long totalCP() {

        return wasteReadModel.countCollectionPointsByTypeNameContaining("Caisse Polybenne");
    }
    @Override
    public Long totalHabitans() {
        return communeRepository.countTotalHabitants();
    }

    @Override
    public DepartmentState departmentState(){

        return DepartmentState.builder()
                .totalPRN(totalPRN())
                .totalBacs(totalBacs())
                .totalPP(totalPP())
                .totalCP(totalCP())
                .totalHabitants(totalHabitans())
                .totalCircuits(totalCircuits())
                .totalBennes(totalBennes())
                .totalDepotoirs(totalDepotoirs())
                .totalCommunes(totalCommunes())
                .build();
    }
}
