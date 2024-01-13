package ucg.collecte.master.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ucg.collecte.master.dto.DepartementDto;
import ucg.collecte.master.model.Departement;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartementMapper {
    DepartementMapper DMP = Mappers.getMapper(DepartementMapper.class);

    DepartementDto modelToDto(Departement department);

    Departement dtoToModel (DepartementDto departmentDto);

    List<DepartementDto> listModelToDto(List<Departement> departments);
}
