package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.DepartmentMapper;
import sonaged.collecte.master.repository.CommuneRepository;
import sonaged.collecte.master.dto.Commune;
import sonaged.collecte.master.mapper.CommuneMapper;
import sonaged.collecte.master.repository.DepartmentRepository;
import sonaged.collecte.master.service.CommuneService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommuneServiceImpl implements CommuneService {
    private final DepartmentRepository departmentRepository;

    private final CommuneRepository communeRepository;


    @Override
    public Commune readCommune(Long communeId) {
       var commune = communeRepository.findById(communeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(communeId)
                ));
        return CommuneMapper.COMP.asDto(commune);
    }

    @Override
    public List<Commune> readAllCommune() {
       var communeList = communeRepository.findByDeletionStatus(sonaged.collecte.master.enums.DeletionStatus.ACTIVE);
        return CommuneMapper.COMP.asListDto(communeList);
    }

    public Page<Commune> readAllCommune(Pageable pageable){
        return communeRepository.findByDeletionStatus(sonaged.collecte.master.enums.DeletionStatus.ACTIVE, pageable).map(CommuneMapper.COMP::asDto);
    }

    @Override
    public Commune createCommune(Commune commune) {

        // P1-7 : le DTO porte désormais `departmentId` et le mapper ignore l'association.
        // Le rattachement se fait donc ici, sur l'ENTITÉ. Department est dans le même
        // contexte (Référentiel territorial) : l'association JPA reste légitime.
        // Au passage, l'ancien code déréférençait `getDepartment()` sans garde et levait
        // une NullPointerException dès qu'une commune était créée sans département.
        var communeEntity = CommuneMapper.COMP.asModel(commune);
        if (commune.getDepartmentId() != null) {
            var department = departmentRepository.findById(commune.getDepartmentId()).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Department with id [%s] not found".formatted(commune.getDepartmentId()))
            );
            communeEntity.setDepartment(department);
        }
        var communeSave = communeRepository.save(communeEntity);
        return CommuneMapper.COMP.asDto(communeSave);
    }


    @Override
    public Commune updateCommune(Long communeId, Commune commune) {
        var existedCommune = communeRepository.findById(communeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commune  with id [%s] not found to update".formatted(communeId)
                ));


        if (commune.getName() != null){
           existedCommune.setName(commune.getName() );
        }
        if (commune.getCode() != null){
            existedCommune.setCode(commune.getCode());
        }
        if (commune.getArea() != null){
            existedCommune.setArea(commune.getArea());
        }
        if (commune.getLength() != null){
            existedCommune.setLength(commune.getLength());
        }
        if (commune.getMen() != null){
            existedCommune.setMen(commune.getMen());
        }
        if (commune.getWomen() != null){
            existedCommune.setWomen(commune.getWomen());
        }
        if (commune.getTotal() != null){
            existedCommune.setTotal(commune.getTotal());
        }

        return CommuneMapper.COMP.asDto(communeRepository.save(existedCommune));
    }


    @Override
    public void deleteCommune(Long communeId) {
        var commune = communeRepository.findById(communeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(communeId)
                ));
         commune.markForDeletion(java.time.LocalDateTime.now()); communeRepository.save(commune); // soft-delete (rétention + purge planifiée)
    }
}
