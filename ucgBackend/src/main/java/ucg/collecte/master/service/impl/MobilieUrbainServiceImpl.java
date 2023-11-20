package ucg.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ucg.collecte.master.dto.MobilieUrbainDto;
import ucg.collecte.master.exception.ResourceNotFoundException;
import ucg.collecte.master.mapper.MobilieUrbainMapper;
import ucg.collecte.master.model.MobilieUrbain;
import ucg.collecte.master.repository.MobilieUrbainRepository;
import ucg.collecte.master.service.MobilieUrbainService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MobilieUrbainServiceImpl implements MobilieUrbainService {

    private final MobilieUrbainRepository mobilieUrbainRepository;
    /**
     * @param mobilieUrbainId 
     * @return
     */
    @Override
    public MobilieUrbainDto getOneMobilieUrbain(Long mobilieUrbainId) {
        MobilieUrbain mobilieUrbain = mobilieUrbainRepository.findById(mobilieUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MobilieUrbain with id [%s] not found ".formatted(mobilieUrbainId)
                ));
        return MobilieUrbainMapper.MUMP.modelToDto(mobilieUrbain);
    }

    /**
     * @return 
     */
    @Override
    public List<MobilieUrbainDto> getAllMobilieUrbain() {
        List<MobilieUrbain> mobilieUrbainList = mobilieUrbainRepository.findAll();
        return MobilieUrbainMapper.MUMP.listModelToDto(mobilieUrbainList);
    }

    /**
     * @param mobilieUrbainDto 
     * @return
     */
    @Override
    public MobilieUrbainDto createOneMobilieUrbain(MobilieUrbainDto mobilieUrbainDto) {
        MobilieUrbain mobilieUrbain = MobilieUrbain.builder()
                .mobilieUrbainCode(mobilieUrbainDto.getMobilieUrbainCode())
                .mobilieUrbainName(mobilieUrbainDto.getMobilieUrbainName())
                .build();
        return MobilieUrbainMapper.MUMP.modelToDto(mobilieUrbainRepository.save(mobilieUrbain));

    }

    /**
     * @param mobilieUrbainId
     * @param mobilieUrbainDto
     * @return
     */
    @Override
    public MobilieUrbainDto updateOneMobilieUrbain(Long mobilieUrbainId, MobilieUrbainDto mobilieUrbainDto) {
        MobilieUrbain existedMobilieUrbain = mobilieUrbainRepository.findById(mobilieUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MobilieUrbain with id [%s] not found to update ".formatted(mobilieUrbainId)
                ));
        if(mobilieUrbainDto.getMobilieUrbainCode() != null) {
            existedMobilieUrbain.setMobilieUrbainCode(mobilieUrbainDto.getMobilieUrbainCode());
        }
        if(mobilieUrbainDto.getMobilieUrbainName() != null) {
            existedMobilieUrbain.setMobilieUrbainName(mobilieUrbainDto.getMobilieUrbainName());
        }
        return MobilieUrbainMapper.MUMP.modelToDto(mobilieUrbainRepository.save(existedMobilieUrbain));

    }

    /**
     * @param mobilieUrbainId 
     */
    @Override
    public void deleteOneMobilieUrbain(Long mobilieUrbainId) {
        MobilieUrbain mobilieUrbain = mobilieUrbainRepository.findById(mobilieUrbainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MobilieUrbain with id [%s] not found ".formatted(mobilieUrbainId)
                ));
        mobilieUrbainRepository.delete(mobilieUrbain);
    }
}
