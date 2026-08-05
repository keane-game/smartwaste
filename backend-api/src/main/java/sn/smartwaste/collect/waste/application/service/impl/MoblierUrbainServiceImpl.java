package sn.smartwaste.collect.waste.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.smartwaste.collect.shared.domain.exception.ResourceNotFoundException;
import sn.smartwaste.collect.waste.application.dto.MoblierUrbain;
import sn.smartwaste.collect.waste.application.mapper.MoblierUrbainMapper;
import sn.smartwaste.collect.waste.domain.repository.MoblierUrbainRepository;
import sn.smartwaste.collect.waste.application.service.MoblierUrbainService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MoblierUrbainServiceImpl implements MoblierUrbainService {

    private final MoblierUrbainRepository moblierUrbainRepository;


    @Override
    public MoblierUrbain readMoblierUrbain(Long moblierUrbainId) {
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
        var savedMoblierUrbain = moblierUrbainRepository.save(MoblierUrbainMapper.MUMP.asModel(moblierUrbain ));
        return MoblierUrbainMapper.MUMP.asDto(moblierUrbainRepository.save(savedMoblierUrbain));

    }


    @Override
    public MoblierUrbain updateMoblierUrbain(Long moblierUrbainId, MoblierUrbain moblierUrbain) {
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
    public void deleteMoblierUrbain(Long moblierUrbainId) {
        var moblierUrbain = moblierUrbainRepository.findById(moblierUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoblierUrbain with id [%s] not found ".formatted(moblierUrbainId)
                ));
        moblierUrbain.markForDeletion(java.time.LocalDateTime.now()); moblierUrbainRepository.save(moblierUrbain); // soft-delete (rétention + purge planifiée)
    }
}
