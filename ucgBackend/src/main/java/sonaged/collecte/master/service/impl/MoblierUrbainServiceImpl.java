package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.dto.MoblierUrbainDto;
import sonaged.collecte.master.mapper.MoblierUrbainMapper;
import sonaged.collecte.master.model.MoblierUrbain;
import sonaged.collecte.master.repository.MoblierUrbainRepository;
import sonaged.collecte.master.service.MoblierUrbainService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MoblierUrbainServiceImpl implements MoblierUrbainService {

    private final MoblierUrbainRepository moblierUrbainRepository;
    /**
     * @param moblierUrbainId
     * @return
     */
    @Override
    public MoblierUrbainDto getOneMoblierUrbain(Long moblierUrbainId) {
        MoblierUrbain moblierUrbain = moblierUrbainRepository.findById(moblierUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoblierUrbain with id [%s] not found ".formatted(moblierUrbainId)
                ));
        return MoblierUrbainMapper.MUMP.modelToDto(moblierUrbain);
    }

    /**
     * @return 
     */
    @Override
    public List<MoblierUrbainDto> getAllMoblierUrbain() {
        List<MoblierUrbain> moblierUrbainList = moblierUrbainRepository.findAll();
        return MoblierUrbainMapper.MUMP.listModelToDto(moblierUrbainList);
    }

    /**
     * @param moblierUrbainDto
     * @return
     */
    @Override
    public MoblierUrbainDto createOneMoblierUrbain(MoblierUrbainDto moblierUrbainDto) {
        MoblierUrbain moblierUrbain = MoblierUrbain.builder()
                .moblierUrbainCode(moblierUrbainDto.getMoblierUrbainCode())
                .moblierUrbainName(moblierUrbainDto.getMoblierUrbainName())
                .build();
        return MoblierUrbainMapper.MUMP.modelToDto(moblierUrbainRepository.save(moblierUrbain));

    }

    /**
     * @param moblierUrbainId
     * @param moblierUrbainDto
     * @return
     */
    @Override
    public MoblierUrbainDto updateOneMoblierUrbain(Long moblierUrbainId, MoblierUrbainDto moblierUrbainDto) {
        MoblierUrbain existedMoblierUrbain = moblierUrbainRepository.findById(moblierUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoblierUrbain with id [%s] not found to update ".formatted(moblierUrbainId)
                ));
        if(moblierUrbainDto.getMoblierUrbainCode() != null) {
            existedMoblierUrbain.setMoblierUrbainCode(moblierUrbainDto.getMoblierUrbainCode());
        }
        if(moblierUrbainDto.getMoblierUrbainName() != null) {
            existedMoblierUrbain.setMoblierUrbainName(moblierUrbainDto.getMoblierUrbainName());
        }
        return MoblierUrbainMapper.MUMP.modelToDto(moblierUrbainRepository.save(existedMoblierUrbain));

    }

    /**
     * @param moblierUrbainId
     */
    @Override
    public void deleteOneMoblierUrbain(Long moblierUrbainId) {
        MoblierUrbain moblierUrbain = moblierUrbainRepository.findById(moblierUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoblierUrbain with id [%s] not found ".formatted(moblierUrbainId)
                ));
        moblierUrbainRepository.delete(moblierUrbain);
    }
}
