package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.repository.CircuitCollectRepository;
import sonaged.collecte.master.dto.CircuitCollect;
import sonaged.collecte.master.mapper.CircuitCollectMapper;
import sonaged.collecte.master.service.CircuitCollectService;

import java.util.List;


@RequiredArgsConstructor
@Service
public class CircuitCollectServiceImpl implements CircuitCollectService {

    private final CircuitCollectRepository circuitCollectRepository;

    @Override
    public CircuitCollect readCircuitCollect(Long circuitCollectId) {
        var circuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitCollectId)
                ));
        return CircuitCollectMapper.CCMP.asDto(circuitCollect);
    }


    @Override
    public List<CircuitCollect> readAllCircuitCollect() {
        var circuitCollectList = circuitCollectRepository.findAll();
        return CircuitCollectMapper.CCMP.asListDto (circuitCollectList);
    }


    @Override
    public CircuitCollect createCircuitCollect(CircuitCollect circuitCollect) {
        var savedCircuitCollect = circuitCollectRepository.save (CircuitCollectMapper.CCMP.asModel (circuitCollect ));
        return CircuitCollectMapper.CCMP.asDto (savedCircuitCollect);
    }


    @Override
    public CircuitCollect updateCircuitCollect(Long circuitCollectId, CircuitCollect circuitCollectDto) {

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
    public void deleteCircuitCollect(Long circuitCollectId) {
        var circuitCollect = circuitCollectRepository.findById(circuitCollectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CircuitCollect with id [%s] not found ".formatted(circuitCollectId)
                ));
        circuitCollectRepository.delete(circuitCollect);
    }
}
