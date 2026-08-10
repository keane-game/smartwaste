package sn.smartwaste.collect.waste.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.domain.repository.CircuitCollectRepository;
import sn.smartwaste.collect.waste.application.dto.CircuitCollect;
import sn.smartwaste.collect.waste.application.mapper.CircuitCollectMapper;
import sn.smartwaste.collect.waste.application.service.CircuitCollectService;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class CircuitCollectServiceImpl implements CircuitCollectService {

    private final CircuitCollectRepository circuitCollectRepository;
    private final CurrentTenantProvider currentTenantProvider;

    @Override
    public CircuitCollect readCircuitCollect(UUID circuitCollectId) {
        var circuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitCollectId)
                ));
        return CircuitCollectMapper.CCMP.asDto(circuitCollect);
    }

    @Override
    public List<CircuitCollect> readAllCircuitCollect() {
        var circuitCollectList = circuitCollectRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return CircuitCollectMapper.CCMP.asListDto (circuitCollectList);
    }

    @Override
    public Page<CircuitCollect> readAllCircuitCollect(Pageable pageable){
        return circuitCollectRepository.findByDeletionStatus (sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE, pageable).map(CircuitCollectMapper.CCMP::asDto);
    }

    @Override
    public CircuitCollect createCircuitCollect(CircuitCollect circuitCollect) {
        var toCreate = CircuitCollectMapper.CCMP.asModel(circuitCollect);
        // ADR-0020 : jamais depuis le DTO client.
        toCreate.setOrganizationId(currentTenantProvider.currentOrganizationId()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune collectivité rattachée au compte courant : impossible de créer un circuit de collecte")));
        var savedCircuitCollect = circuitCollectRepository.save(toCreate);
        return CircuitCollectMapper.CCMP.asDto (savedCircuitCollect);
    }


    @Override
    public CircuitCollect updateCircuitCollect(UUID circuitCollectId, CircuitCollect circuitCollectDto) {

        var existedCircuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found to update ".formatted(circuitCollectId)
                ));

        if(circuitCollectDto.getName() != null) {
            existedCircuitCollect.setName(circuitCollectDto.getName());
        }
        if(circuitCollectDto.getCat() != null) {
            existedCircuitCollect.setCat(circuitCollectDto.getCat());
        }
        if(circuitCollectDto.getCode() != null) {
            existedCircuitCollect.setCode(circuitCollectDto.getCode());
        }
        if(circuitCollectDto.getLatiPointA() != null) {
            existedCircuitCollect.setLatiPointA(circuitCollectDto.getLatiPointA());
        }
        if(circuitCollectDto.getLatiPointD() != null) {
            existedCircuitCollect.setLatiPointD(circuitCollectDto.getLatiPointD());
        }
        if(circuitCollectDto.getLongPointA() != null) {
            existedCircuitCollect.setLongPointA(circuitCollectDto.getLongPointA());
        }
        if(circuitCollectDto.getLongPointD() != null) {
            existedCircuitCollect.setLongPointD(circuitCollectDto.getLongPointD());
        }
        if(circuitCollectDto.getLength() != null) {
            existedCircuitCollect.setLength(circuitCollectDto.getLength());
        }
        if(circuitCollectDto.getFrequency() != null) {
            existedCircuitCollect.setFrequence(circuitCollectDto.getFrequency());
        }
        if(circuitCollectDto.getSection() != null) {
            existedCircuitCollect.setSection(circuitCollectDto.getSection());
        }
        if(circuitCollectDto.getSectection() != null) {
            existedCircuitCollect.setSectection(circuitCollectDto.getSectection());
        }
        if(circuitCollectDto.getRotation() != null) {
            existedCircuitCollect.setRotation(circuitCollectDto.getRotation());
        }
        if(circuitCollectDto.getType() != null) {
            existedCircuitCollect.setType(circuitCollectDto.getType());
        }
        return CircuitCollectMapper.CCMP.asDto (circuitCollectRepository.save (existedCircuitCollect));
    }


    @Override
    public void deleteCircuitCollect(UUID circuitCollectId) {
        var circuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitCollectId)
                ));
        circuitCollect.markForDeletion(java.time.LocalDateTime.now()); circuitCollectRepository.save(circuitCollect); // soft-delete (rétention + purge planifiée)
    }
}
