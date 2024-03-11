package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.dto.CoordinateDto;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.CoordinateMapper;
import sonaged.collecte.master.model.Coordinate;
import sonaged.collecte.master.repository.CoordinateRepository;
import sonaged.collecte.master.service.CoordinateService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CoordinateServiceImpl implements CoordinateService {


    private final CoordinateRepository coordinateRepository;
    /**
     * @param coordinateId
     * @return
     */
    @Override
    public CoordinateDto getOneCoordinate(Long coordinateId) {
        Coordinate coordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        return CoordinateMapper.COORDINATE_MAPPER.modelToDto(coordinate);
    }

    /**
     * @return 
     */
    @Override
    public List<CoordinateDto> getAllCoordinate() {
        List<Coordinate> coordinateList = coordinateRepository.findAll();
        return CoordinateMapper.COORDINATE_MAPPER.listModelToDto(coordinateList);
    }

    /**
     * @param coordinateDto
     * @return
     */
    @Override
    public CoordinateDto createOneCoordinate(CoordinateDto coordinateDto) {
        Coordinate coordinate = Coordinate.builder()
                .latitude(coordinateDto.getLatitude())
                .longitude(coordinateDto.getLongitude())
                .altitude(coordinateDto.getAltitude())
                .build();

         coordinate = coordinateRepository.save(coordinate);
        return CoordinateMapper.COORDINATE_MAPPER.modelToDto(coordinate);
    }

    /**
     * @param coordinateId
     * @param coordinateDto
     * @return
     */
    @Override
    public CoordinateDto updateOneCoordinate(Long coordinateId, CoordinateDto coordinateDto) {
        Coordinate existedCoordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        if (coordinateDto.getLatitude() != null){
            existedCoordinate.setLatitude(coordinateDto.getLatitude());
        }
        if (coordinateDto.getLongitude() != null){
            existedCoordinate.setLongitude(coordinateDto.getLongitude());
        }
        if (coordinateDto.getAltitude() != null){
            existedCoordinate.setAltitude(coordinateDto.getAltitude());
        }
        return CoordinateMapper.COORDINATE_MAPPER.modelToDto(existedCoordinate);
    }

    /**
     * @param coordinateId
     */
    @Override
    public void deleteOneCoordinate(Long coordinateId) {
        Coordinate coordinate = coordinateRepository.findById(coordinateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinate with id [%s] not found ".formatted(coordinateId)
                ));
        coordinateRepository.delete(coordinate);
    }
}
