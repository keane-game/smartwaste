package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.dto.Depotoir;
import sonaged.collecte.master.mapper.CommuneMapper;
import sonaged.collecte.master.mapper.CoordinateMapper;
import sonaged.collecte.master.mapper.DepotoirMapper;
import sonaged.collecte.master.model.GeometryEntity;
import sonaged.collecte.master.repository.DepotoirRepository;
import sonaged.collecte.master.repository.GeometryRepository;
import sonaged.collecte.master.repository.QuartierRepository;
import sonaged.collecte.master.repository.TypeDepotoirRepository;
import sonaged.collecte.master.service.DepotoirService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepotoirServiceImpl implements DepotoirService {
    private final GeometryRepository geometryRepository;
    private final QuartierRepository quartierRepository;
    private final TypeDepotoirRepository typeDepotoirRepository;

    private final DepotoirRepository depotoirRepository;

    @Override
    public Depotoir readDepotoir(Long depotoirId) {
        var depotoir  = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depotoir with id [%s] not found ".formatted(depotoirId)
                ));
        return DepotoirMapper.DETMP.asDto(depotoir);
    }


    @Override
    public List<Depotoir> readAllDepotoir() {
        var depotoirList = depotoirRepository.findAll();
        return DepotoirMapper.DETMP.asListDto(depotoirList);
    }

    @Override
    public Page<Depotoir> readAllDepotoir(Pageable pageable) {
        //return depotoirRepository.findAll (pageable).map (DepotoirMapper.DETMP::asDto);
       return readAllDepotoirs(pageable);
    }
    public Page<Depotoir> readAllDepotoirs(Pageable pageable) {
        return depotoirRepository.findAll(pageable)
                .map(depotoir -> {
                    Depotoir dto = DepotoirMapper.DETMP.asDto(depotoir);
                    Long geometryId = dto.getGeometry().getGeometryId();

                    geometryRepository.findById(geometryId).ifPresent(geometry -> {
                        dto.setCoordinates(CoordinateMapper.CODMP.asListDto(geometry.getCoordinates()));
                    });

                    return dto;
                });
    }
    @Override
    public Depotoir createDepotoir(Depotoir depotoir) {
        var t_depotId = depotoir.getTypeDepotoir().getTypeDepotoirId ();
     /*   if(t_depotId != null && quartierId != null) {
            var typeDepotoir = typeDepotoirRepository.findById (t_depotId).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
            var quartier = quartierRepository.findById (quartierId).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
           // depotoir.setTypeDepotoir (typeDepotoir);
           // depotoir.setQuartier (quartier);
        }*/
        var savedDepotoir = depotoirRepository.save(DepotoirMapper.DETMP.asModel(depotoir));
        return DepotoirMapper.DETMP.asDto(depotoirRepository.save(savedDepotoir));
    }


    @Override
    public Depotoir updateDepotoir(Long depotoirId, Depotoir depotoir) {
        var existedDepotoir = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depotoir with id [%s] not found to update ".formatted(depotoirId)
                ));

        existedDepotoir.setAddress(depotoir.getAddress());
        var updateDepotoir = depotoirRepository.save(existedDepotoir);
        return DepotoirMapper.DETMP.asDto(updateDepotoir);
    }


    @Override
    public void deleteDepotoir(Long depotoirId) {
        var existeddepotoir = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depotoir with id [%s] not found to update ".formatted(depotoirId)
                ));
        depotoirRepository.delete(existeddepotoir);
    }


}
