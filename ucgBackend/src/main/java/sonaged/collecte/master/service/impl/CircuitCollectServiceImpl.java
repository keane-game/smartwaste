package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.CircuitCollectRepository;
import sonaged.collecte.master.dto.CircuitCollectDto;
import sonaged.collecte.master.mapper.CircuitCollectMapper;
import sonaged.collecte.master.model.CircuitCollect;
import sonaged.collecte.master.service.CircuitCollectService;

import java.util.List;


@RequiredArgsConstructor
@Service
public class CircuitCollectServiceImpl implements CircuitCollectService {

    private final CircuitCollectRepository circuitCollectRepository;

    /**
     * @param circuitCollectId 
     * @return
     */
    @Override
    public CircuitCollectDto getOneCircuitCollect(Long circuitCollectId) {
        CircuitCollect circuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitCollectId)
                ));
        return CircuitCollectMapper.CCMP.modelToDto(circuitCollect);
    }

    /**
     * @return 
     */
    @Override
    public List<CircuitCollectDto> getAllCircuitCollect() {
        List<CircuitCollect> circuitCollectList = circuitCollectRepository.findAll();
        return CircuitCollectMapper.CCMP.listModelToDto(circuitCollectList);
    }

    /**
     * @param circuitCollectDto 
     * @return
     */
    @Override
    public CircuitCollectDto createOneCircuitCollect(CircuitCollectDto circuitCollectDto) {
        CircuitCollect circuitCollect = CircuitCollect.builder()
                .circuitcollectName(circuitCollectDto.getCircuitcollectName())
                .circuitcollectCat(circuitCollectDto.getCircuitcollectCat())
                .circuitcollectCode(circuitCollectDto.getCircuitcollectCode())
                .circuitcollectLatiPointA(circuitCollectDto.getCircuitcollectLatiPointA())
                .circuitcollectLatiPointD(circuitCollectDto.getCircuitcollectLatiPointD())
                .circuitcollectLongPointA(circuitCollectDto.getCircuitcollectLongPointA())
                .circuitcollectLongPointD(circuitCollectDto.getCircuitcollectLongPointD())
                .circuitcollectLength(circuitCollectDto.getCircuitcollectLength())
                .circuitcollectFrequence(circuitCollectDto.getCircuitcollectFrequence())
                .circuitcollectSection(circuitCollectDto.getCircuitcollectSection())
                .circuitcollectSectection(circuitCollectDto.getCircuitcollectSectection())
                .circuitcollectRotation(circuitCollectDto.getCircuitcollectRotation())
                .circuitcollectType(circuitCollectDto.getCircuitcollectType())
                .build();
        return CircuitCollectMapper.CCMP.modelToDto(circuitCollectRepository.save(circuitCollect));
    }

    /**
     * @param circuitCollectId
     * @param circuitCollectDto
     * @return
     */
    @Override
    public CircuitCollectDto updateOneCircuitCollect(Long circuitCollectId, CircuitCollectDto circuitCollectDto) {

        CircuitCollect existedCircuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found to update ".formatted(circuitCollectId)
                ));
        if(circuitCollectDto.getCircuitcollectName() != null) {
            existedCircuitCollect.setCircuitcollectName(circuitCollectDto.getCircuitcollectName());
        }
        if(circuitCollectDto.getCircuitcollectCat() != null) {
            existedCircuitCollect.setCircuitcollectCat(circuitCollectDto.getCircuitcollectCat());
        }
        if(circuitCollectDto.getCircuitcollectCode() != null) {
            existedCircuitCollect.setCircuitcollectCode(circuitCollectDto.getCircuitcollectCode());
        }
        if(circuitCollectDto.getCircuitcollectLatiPointA() != null) {
            existedCircuitCollect.setCircuitcollectLatiPointA(circuitCollectDto.getCircuitcollectLatiPointA());
        }
        if(circuitCollectDto.getCircuitcollectLatiPointD() != null) {
            existedCircuitCollect.setCircuitcollectLatiPointD(circuitCollectDto.getCircuitcollectLatiPointD());
        }
        if(circuitCollectDto.getCircuitcollectLongPointA() != null) {
            existedCircuitCollect.setCircuitcollectLongPointA(circuitCollectDto.getCircuitcollectLongPointA());
        }
        if(circuitCollectDto.getCircuitcollectLongPointD() != null) {
            existedCircuitCollect.setCircuitcollectLongPointD(circuitCollectDto.getCircuitcollectLongPointD());
        }
        if(circuitCollectDto.getCircuitcollectLength() != null) {
            existedCircuitCollect.setCircuitcollectLength(circuitCollectDto.getCircuitcollectLength());
        }
        if(circuitCollectDto.getCircuitcollectFrequence() != null) {
            existedCircuitCollect.setCircuitcollectFrequence(circuitCollectDto.getCircuitcollectFrequence());
        }
        if(circuitCollectDto.getCircuitcollectSection() != null) {
            existedCircuitCollect.setCircuitcollectSection(circuitCollectDto.getCircuitcollectSection());
        }
        if(circuitCollectDto.getCircuitcollectSectection() != null) {
            existedCircuitCollect.setCircuitcollectSectection(circuitCollectDto.getCircuitcollectSectection());
        }
        if(circuitCollectDto.getCircuitcollectRotation() != null) {
            existedCircuitCollect.setCircuitcollectRotation(circuitCollectDto.getCircuitcollectRotation());
        }
        if(circuitCollectDto.getCircuitcollectType() != null) {
            existedCircuitCollect.setCircuitcollectType(circuitCollectDto.getCircuitcollectType());
        }
        return CircuitCollectMapper.CCMP.modelToDto(existedCircuitCollect);
    }

    /**
     * @param circuitCollectId 
     */
    @Override
    public void deleteOneCircuitCollect(Long circuitCollectId) {
        CircuitCollect circuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitCollectId)
                ));
        circuitCollectRepository.delete(circuitCollect);
    }
}
