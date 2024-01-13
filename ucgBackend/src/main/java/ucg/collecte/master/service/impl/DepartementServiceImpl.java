package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.DepartementDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.DepartementMapper;
import ucg.collecte.master.mapper.UserMapper;
import ucg.collecte.master.model.Departement;
import ucg.collecte.master.model.User;
import ucg.collecte.master.repository.DepartementRepository;
import ucg.collecte.master.service.DepartementService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepartementServiceImpl implements DepartementService {

    private final DepartementRepository departementRepository;
    /**
     * @param departementId
     * @return
     */
    @Override
    public DepartementDto getOneDepartement(Long departementId) {
        Departement departement  = departementRepository.findById(departementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(departementId)
                ));
        return DepartementMapper.DMP.modelToDto(departement);
    }

    /**
     * @return 
     */
    @Override
    public List<DepartementDto> getAllDepartement() {
        List<Departement> departementList = departementRepository.findAll();
        return DepartementMapper.DMP.listModelToDto(departementList);
    }

    /**
     * @param departementDto
     * @return
     */
    @Override
    public DepartementDto createOneDepartement(DepartementDto departementDto) {
        Departement departement = Departement.builder()
                .departementName(departementDto.getDepartementName())
                .departementCode(departementDto.getDepartementCode())
                .build();
        Departement departementSave = departementRepository.save(departement);
        return DepartementMapper.DMP.modelToDto(departementSave);
    }

    /**
     * @param DepartementId
     * @param departementDto
     * @return
     */
    @Override
    public DepartementDto updateOneDepartement(Long DepartementId, DepartementDto departementDto) {
        return null;
    }

    /**
     * @param departementId
     */
    @Override
    public void deleteOneDepartement(Long departementId) {

    }
}
