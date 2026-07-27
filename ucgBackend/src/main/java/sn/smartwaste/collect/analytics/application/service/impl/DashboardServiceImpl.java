package sn.smartwaste.collect.analytics.application.service.impl;

import sn.smartwaste.collect.analytics.application.dto.DepartmentState;
import sn.smartwaste.collect.analytics.application.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.repository.*;
// Le référentiel territorial a migré vers son module dédié : l'import « étoile » sur le package
// hérité ne couvre plus ses repositories, importés nommément ci-dessous.
import sn.smartwaste.collect.territory.domain.repository.CommuneRepository;
import sn.smartwaste.collect.territory.domain.repository.CoordinateRepository;
import sn.smartwaste.collect.territory.domain.repository.DepartmentRepository;
import sn.smartwaste.collect.territory.domain.repository.GeometryRepository;
import sn.smartwaste.collect.territory.domain.repository.QuartierRepository;
import sn.smartwaste.collect.territory.domain.repository.RegionRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardServiceImpl implements DashboardService {

    final CircuitRepository circuitRepository;
    final CommuneRepository communeRepository;
    final CircuitCollectRepository circuitCollectRepository;
    final CircuitBalayageRepository circuitBalayageRepository;
    final DepartmentRepository departmentRepository;
    final RegionRepository regionRepository;
    final QuartierRepository quartierRepository;
    final TypeDepotoirRepository typeDepotoirRepository;
    final DepotoirRepository depotoirRepository;
    final GeometryRepository geometryRepository;
    final CoordinateRepository coordinateRepository;
    final MoblierUrbainRepository moblierUrbainRepository;
    final UserRepository userRepository;

    @Override
    public Long totalCommunes() {
        return communeRepository.count();
    }

    @Override
    public Long totalDepotoirs() {
        return depotoirRepository.count();
    }

    @Override
    public Long totalBennes() {
        return moblierUrbainRepository.count();
    }

    @Override
    public Long totalCircuits() {
        return circuitCollectRepository.count() + circuitBalayageRepository.count();
    }

    @Override
    public Long totalBacs() {
        var bacs =  depotoirRepository.findByTypeDepotoir_NameContainingIgnoreCase("Bac");
        return (long) bacs.size();
    }

    /*
        PNR: Point de regroupement normalisée
    */
    @Override
    public Long totalPRN() {
        var pnr =  depotoirRepository.findByTypeDepotoir_NameContainingIgnoreCase("PRN");
        return (long) pnr.size();
    }

    /*
        PP: Point  Propres
    */
    @Override
    public Long totalPP() {

        var pointPropres =  depotoirRepository.findByTypeDepotoir_NameContainingIgnoreCase("PP");
        return (long) pointPropres.size();
    }

    /*
     CP: Caisses  Polybennes
   */
    @Override
    public Long totalCP() {

        var caissePolybenne =  depotoirRepository.findByTypeDepotoir_NameContainingIgnoreCase("Caisse Polybenne");
        return (long) caissePolybenne.size();
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
