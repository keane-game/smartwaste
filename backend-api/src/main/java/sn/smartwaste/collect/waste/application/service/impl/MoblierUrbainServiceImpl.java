package sn.smartwaste.collect.waste.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.dto.MoblierUrbain;
import sn.smartwaste.collect.waste.application.mapper.MoblierUrbainMapper;
import sn.smartwaste.collect.waste.domain.repository.MoblierUrbainRepository;
import sn.smartwaste.collect.waste.application.service.MoblierUrbainService;
import sn.smartwaste.collect.tenant.application.api.CurrentTenantProvider;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MoblierUrbainServiceImpl implements MoblierUrbainService {

    private final MoblierUrbainRepository moblierUrbainRepository;
    private final CurrentTenantProvider currentTenantProvider;


    @Override
    public MoblierUrbain readMoblierUrbain(UUID moblierUrbainId) {
        var moblierUrbain = moblierUrbainRepository.findById(moblierUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoblierUrbain with id [%s] not found ".formatted(moblierUrbainId)
                ));
        return MoblierUrbainMapper.MUMP.asDto(moblierUrbain);
    }


    @Override
    public List<MoblierUrbain> readAllMoblierUrbain() {
        var moblierUrbainList = moblierUrbainRepository.findByDeletionStatus(sn.smartwaste.collect.shared.domain.model.DeletionStatus.ACTIVE);
        return MoblierUrbainMapper.MUMP.asListDto(moblierUrbainList);
    }


    @Override
    public MoblierUrbain createMoblierUrbain(MoblierUrbain moblierUrbain) {
        var toCreate = MoblierUrbainMapper.MUMP.asModel(moblierUrbain);
        // ADR-0020 : jamais depuis le DTO client.
        toCreate.setOrganizationId(currentTenantProvider.currentOrganizationId()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune collectivité rattachée au compte courant : impossible de créer un mobilier urbain")));
        var savedMoblierUrbain = moblierUrbainRepository.save(toCreate);
        return MoblierUrbainMapper.MUMP.asDto(savedMoblierUrbain);
    }


    @Override
    public MoblierUrbain updateMoblierUrbain(UUID moblierUrbainId, MoblierUrbain moblierUrbain) {
        var existedMoblierUrbain = moblierUrbainRepository.findById(moblierUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoblierUrbain with id [%s] not found to update ".formatted(moblierUrbainId)
                ));
        if(moblierUrbain.getCode() != null) {
            existedMoblierUrbain.setCode(moblierUrbain.getCode());
        }
        if(moblierUrbain.getName() != null) {
            existedMoblierUrbain.setName(moblierUrbain.getName());
        }
        return MoblierUrbainMapper.MUMP.asDto(moblierUrbainRepository.save(existedMoblierUrbain));

    }


    @Override
    public void deleteMoblierUrbain(UUID moblierUrbainId) {
        var moblierUrbain = moblierUrbainRepository.findById(moblierUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoblierUrbain with id [%s] not found ".formatted(moblierUrbainId)
                ));
        moblierUrbain.markForDeletion(java.time.LocalDateTime.now()); moblierUrbainRepository.save(moblierUrbain); // soft-delete (rétention + purge planifiée)
    }
}
