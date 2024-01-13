package ucg.collecte.master.service;

import ucg.collecte.master.dto.DepartementDto;

import java.util.List;

public interface DepartementService {
    DepartementDto getOneDepartement(Long departmentId);

    List<DepartementDto> getAllDepartement();

    DepartementDto createOneDepartement(DepartementDto departmentDto);

    DepartementDto updateOneDepartement(Long DepartementId, DepartementDto departmentDto);
    void deleteOneDepartement(Long departmentId);
}
