package sn.smartwaste.collect.waste.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.dto.TypeDepotoir;
import sn.smartwaste.collect.waste.application.mapper.TypeDepotoirMapper;
import sn.smartwaste.collect.waste.domain.repository.TypeDepotoirRepository;
import sn.smartwaste.collect.waste.application.service.TypeDepotoirService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class TypeDepotoirServiceImpl implements TypeDepotoirService {

    private final TypeDepotoirRepository typeDepotoirRepository;


    @Override
    public TypeDepotoir readTypeDepotoir(UUID typeDepotoirId) {
        var typeDepotoir  = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TypeDepotoir with id [%s] not found ".formatted(typeDepotoirId)
                ));

        return TypeDepotoirMapper.TDMP.asDto(typeDepotoir);
    }


    @Override
    public List<TypeDepotoir> readAllTypeDepotoir() {
        var typeDepotoirList = typeDepotoirRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return TypeDepotoirMapper.TDMP.asListDto(typeDepotoirList);
    }

    @Override
    public Page<TypeDepotoir> readAllTypeDepotoir(Pageable pageable) {
        return typeDepotoirRepository.findByDeletionStatus(
                sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map(TypeDepotoirMapper.TDMP::asDto);
    }


    @Override
    public TypeDepotoir createTypeDepotoir(TypeDepotoir typeDepotoir) {
        var savedTypeDepotoir = typeDepotoirRepository.save(TypeDepotoirMapper.TDMP.asModel(typeDepotoir));
        return TypeDepotoirMapper.TDMP.asDto (savedTypeDepotoir);
    }


    @Override
    public TypeDepotoir updateTypeDepotoir(UUID typeDepotoirId, TypeDepotoir typeDepotoirDto) {
        var existedtypeDepotoir = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depotoir with id [%s] not found to update ".formatted(typeDepotoirId)
                ));
        if (!Objects.equals(existedtypeDepotoir.getTypeDepotoirId(), typeDepotoirDto.getTypeDepotoirId ())) {
            throw new ResourceNotFoundException(
                    "Corrupted body request or route");
        }
        existedtypeDepotoir.setName(typeDepotoirDto.getName());
        return TypeDepotoirMapper
                .TDMP.asDto(
                    typeDepotoirRepository.save(existedtypeDepotoir));
    }


    @Override
    public void deleteTypeDepotoir(UUID typeDepotoirId) {
        var existedtypeDepotoir = typeDepotoirRepository.findById(typeDepotoirId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Type de depotoir with id [%s] not found to update ".formatted(typeDepotoirId)
                ));
         existedtypeDepotoir.markForDeletion(java.time.LocalDateTime.now()); typeDepotoirRepository.save(existedtypeDepotoir); // soft-delete (rétention + purge planifiée)

    }
}
