package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.dto.DepotoirDto;
import sonaged.collecte.master.mapper.DepotoirMapper;
import sonaged.collecte.master.model.Depotoir;
import sonaged.collecte.master.repository.DepotoirRepository;
import sonaged.collecte.master.service.DepotoirService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepotoirServiceImpl implements DepotoirService {

    private final DepotoirRepository depotoirRepository;
    /**
     * @param depotoirId
     * @return
     */
    @Override
    public DepotoirDto getOneDepotoir(Long depotoirId) {
        Depotoir depotoir  = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Depotoir with id [%s] not found ".formatted(depotoirId)
                ));
        return DepotoirMapper.DETMP.modelToDto(depotoir);
    }

    /**
     * @return
     */
    @Override
    public List<DepotoirDto> getAllDepotoir() {
        List<Depotoir> depotoirList = depotoirRepository.findAll();
        return DepotoirMapper.DETMP.listModelToDto(depotoirList);
    }

    /**
     * @param depotoirDto
     * @return
     */
    @Override
    public DepotoirDto createOneDepotoir(DepotoirDto depotoirDto) {
        Depotoir depotoir  = Depotoir.builder()
                .depotoirAddress(depotoirDto.getDepotoirAddress())
                .build();
        return DepotoirMapper.DETMP.modelToDto(depotoirRepository.save(depotoir));
    }

    /**
     * @param depotoirId
     * @param depotoirDto
     * @return
     */
    @Override
    public DepotoirDto updateOneDepotoir(Long depotoirId, DepotoirDto depotoirDto) {
        Depotoir existedDepotoir = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        " de depotoir with id [%s] not found to update ".formatted(depotoirId)
                ));
        if (!Objects.equals(existedDepotoir.getDepotoirId(), depotoirDto.getDepotoirId())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        existedDepotoir.setDepotoirAddress(depotoirDto.getDepotoirAddress());
        Depotoir updateDepotoir = depotoirRepository.save(existedDepotoir);
        return DepotoirMapper.DETMP.modelToDto(updateDepotoir);
    }

    /**
     * @param depotoirId
     */
    @Override
    public void deleteOneDepotoir(Long depotoirId) {
        Depotoir existeddepotoir = depotoirRepository.findById(depotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        " de depotoir with id [%s] not found to update ".formatted(depotoirId)
                ));
        depotoirRepository.delete(existeddepotoir);
    }
}
