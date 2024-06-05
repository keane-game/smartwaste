package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.Coordinate;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.CoordinateMapper;
import sonaged.collecte.master.repository.CoordinateRepository;
import sonaged.collecte.master.service.CoordinateService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CoordinateServiceImpl implements CoordinateService {


    private final CoordinateRepository coordinateRepository;


    @Override
    public Coordinate readCoordinate(Long coordinateId) {
        var coordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        return CoordinateMapper.CODMP.asDto(coordinate);
    }


    @Override
    public List<Coordinate> readAllCoordinate() {
        var coordinateList = coordinateRepository.findAll();
        return CoordinateMapper.CODMP.asListDto(coordinateList);
    }


    @Override
    public Coordinate createCoordinate(Coordinate coordinate) {
        var savedCoordinate =  coordinateRepository.save(CoordinateMapper.CODMP.asModel (coordinate));
        return CoordinateMapper.CODMP.asDto (savedCoordinate);
    }


    @Override
    public Coordinate updateCoordinate(Long coordinateId, Coordinate coordinate) {
        var existedCoordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        if (coordinate.getLatitude() != null){
            existedCoordinate.setLatitude(coordinate.getLatitude());
        }
        if (coordinate.getLongitude() != null){
            existedCoordinate.setLongitude(coordinate.getLongitude());
        }
        if (coordinate.getAltitude() != null){
            existedCoordinate.setAltitude(coordinate.getAltitude());
        }
        return CoordinateMapper.CODMP.asDto(existedCoordinate);
    }


    @Override
    public void deleteCoordinate(Long coordinateId) {
        var coordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        coordinateRepository.delete(coordinate);
    }
}
