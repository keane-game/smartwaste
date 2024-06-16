package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.repository.*;
import sonaged.collecte.master.service.DashboardService;

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
    public Long totalCommune() {
        return 0L;
    }

    @Override
    public Long totalDepotoir() {
        return 0L;
    }

    @Override
    public Long totalBennes() {
        return 0L;
    }

    @Override
    public Long totalCircuits() {
        return 0L;
    }

    @Override
    public Long totalBacs() {
        return 0L;
    }

    @Override
    public Long totalPnr() {
        return 0L;
    }

    @Override
    public Long totalHabitans() {
        return 0L;
    }
}
