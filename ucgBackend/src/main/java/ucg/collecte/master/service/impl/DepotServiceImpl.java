package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.DepotDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.DepotMapper;
import ucg.collecte.master.model.Depot;
import ucg.collecte.master.repository.DepotRepository;
import ucg.collecte.master.service.DepotService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepotServiceImpl implements DepotService {

    private final DepotRepository depotRepository;
    /**
     * @param depotId
     * @return
     */
    @Override
    public DepotDto getOneDepot(Long depotId) {
        Depot depot  = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depot with id [%s] not found ".formatted(depotId)
                ));
        return DepotMapper.DETMP.modelToDto(depot);
    }

    /**
     * @return
     */
    @Override
    public List<DepotDto> getAllDepot() {
        List<Depot> depotList = depotRepository.findAll();
        return DepotMapper.DETMP.listModelToDto(depotList);
    }

    /**
     * @param depotDto
     * @return
     */
    @Override
    public DepotDto createOneDepot(DepotDto depotDto) {
        Depot depot  = Depot.builder()
                .depotAddress(depotDto.getDepotAddress())
                .build();
        return DepotMapper.DETMP.modelToDto(depotRepository.save(depot));
    }

    /**
     * @param depotId
     * @param depotDto
     * @return
     */
    @Override
    public DepotDto updateOneDepot(Long depotId, DepotDto depotDto) {
        Depot existedDepot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        " de depot with id [%s] not found to update ".formatted(depotId)
                ));
        if (!Objects.equals(existedDepot.getDepotId(), depotDto.getDepotId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        existedDepot.setDepotAddress(depotDto.getDepotAddress());
        Depot updateDepot = depotRepository.save(existedDepot);
        return DepotMapper.DETMP.modelToDto(updateDepot);
    }

    /**
     * @param depotId
     */
    @Override
    public void deleteOneDepot(Long depotId) {
        Depot existeddepot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        " de depot with id [%s] not found to update ".formatted(depotId)
                ));
        depotRepository.delete(existeddepot);
    }
}
