package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.DepartmentMapper;
import sonaged.collecte.master.mapper.RegionMapper;
import sonaged.collecte.master.repository.CommuneRepository;
import sonaged.collecte.master.dto.CommuneDto;
import sonaged.collecte.master.mapper.CommuneMapper;
import sonaged.collecte.master.model.Commune;
import sonaged.collecte.master.repository.DepartmentRepository;
import sonaged.collecte.master.service.CommuneService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommuneServiceImpl implements CommuneService {
    private final DepartmentRepository departmentRepository;

    private final CommuneRepository communeRepository;
    /**
     * @param communeId 
     * @return
     */
    @Override
    public CommuneDto getOneCommune(Long communeId) {
       Commune commune = communeRepository.findById(communeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(communeId)
                ));
        return CommuneMapper.COMP.modelToDto(commune);
    }

    /**
     * @return 
     */
    @Override
    public List<CommuneDto> getAllCommune() {
        List <Commune> communeList = communeRepository.findAll();
        return CommuneMapper.COMP.listModelToDto(communeList);
    }

    /**
     * @param communeDto 
     * @return
     */
    @Override
    public CommuneDto createOneCommune(CommuneDto communeDto) {

        if(communeDto.getDepartment ().getDepartmentId () != null) {
            var department = departmentRepository.findById (communeDto.getDepartment ().getDepartmentId () ).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
            communeDto.setDepartment ( DepartmentMapper.DMP.modelToDto(department));
        }
        var communeSave = communeRepository.save(CommuneMapper.COMP.dtoToModel (communeDto));
        return CommuneMapper.COMP.modelToDto(communeSave);
    }

    /**
     * @param communeId 
     * @param communeDto
     * @return
     */
    @Override
    public CommuneDto updateOneCommune(Long communeId, CommuneDto communeDto) {
        Commune existedCommune = communeRepository.findById(communeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commune  with id [%s] not found to update".formatted(communeId)
                ));


        if (communeDto.getCommuneName() != null){
           existedCommune.setCommuneName(communeDto.getCommuneName() );
        }
        if (communeDto.getCommuneCode() != null){
            existedCommune.setCommuneCode(communeDto.getCommuneCode());
        }
        if (communeDto.getCommuneArea() != null){
            existedCommune.setCommuneArea(communeDto.getCommuneArea());
        }
        if (communeDto.getCommuneLength() != null){
            existedCommune.setCommuneLength(communeDto.getCommuneLength());
        }
        if (communeDto.getManResident() != null){
            existedCommune.setManResident(communeDto.getManResident());
        }
        if (communeDto.getWomanResident() != null){
            existedCommune.setWomanResident(communeDto.getWomanResident());
        }
        if (communeDto.getResidentTotal() != null){
            existedCommune.setResidentTotal(communeDto.getResidentTotal());
        }

        Commune communeUpdate = communeRepository.save(existedCommune);
        return CommuneMapper.COMP.modelToDto(communeUpdate);
    }

    /**
     * @param communeId 
     */
    @Override
    public void deleteOneCommune(Long communeId) {
        Commune commune = communeRepository.findById(communeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(communeId)
                ));
         communeRepository.delete(commune);
    }
}
