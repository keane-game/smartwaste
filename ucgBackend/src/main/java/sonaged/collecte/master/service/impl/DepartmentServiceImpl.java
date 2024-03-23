package sonaged.collecte.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sonaged.collecte.master.exception.ResourceNotFoundException;
import sonaged.collecte.master.mapper.RegionMapper;
import sonaged.collecte.master.model.Geometry;
import sonaged.collecte.master.model.Region;
import sonaged.collecte.master.repository.DepartmentRepository;
import sonaged.collecte.master.dto.DepartmentDto;
import sonaged.collecte.master.mapper.DepartmentMapper;
import sonaged.collecte.master.model.Department;
import sonaged.collecte.master.repository.GeometryRepository;
import sonaged.collecte.master.repository.RegionRepository;
import sonaged.collecte.master.service.DepartmentService;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    private final RegionRepository regionRepository;

    private final DepartmentRepository departmentRepository;
    private final GeometryRepository geometryRepository;
    /**
     * @param departmentId
     * @return
     */
    @Override
    public DepartmentDto getOneDepartment(Long departmentId) {
        Department department  = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id [%s] not found ".formatted(departmentId)
                ));
        return DepartmentMapper.DMP.modelToDto(department);
    }

    /**
     * @return 
     */
    @Override
    public List<DepartmentDto> getAllDepartment() {

        List<Department> departmentList = departmentRepository.findAll();
        return DepartmentMapper.DMP.listModelToDto(departmentList);
    }

    /**
     * @param departmentDto
     * @return
     */
    @Override
    public DepartmentDto createOneDepartment(DepartmentDto departmentDto) {
        if(departmentDto.getRegion ().getRegionId () != null) {
            var region = regionRepository.findById (departmentDto.getRegion ( ).getRegionId ( )).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
            departmentDto.setRegion (RegionMapper.RMP.modelToDto (region));
        }

        var departmentSave = departmentRepository.save(DepartmentMapper.DMP.dtoToModel (departmentDto));
        return DepartmentMapper.DMP.modelToDto(departmentSave);
    }

    /**
     * @param departmentId
     * @param departmentDto
     * @return
     */
    @Override
    public DepartmentDto updateOneDepartment(Long departmentId, DepartmentDto departmentDto) {
        Department depDB = departmentRepository.findById(departmentId).get();
        if(Objects.nonNull(departmentDto.getDepartmentName()) &&
                !"".equalsIgnoreCase(departmentDto.getDepartmentName())) {
            depDB.setDepartmentName(departmentDto.getDepartmentName());
        }
        if(Objects.nonNull(departmentDto.getDepartmentCode()) &&
                !"".equalsIgnoreCase(departmentDto.getDepartmentCode())) {
            depDB.setDepartmentCode(departmentDto.getDepartmentCode());
        }
        return DepartmentMapper.DMP.modelToDto(departmentRepository.save(depDB));
    }

    /**
     * @param departmentId
     */
    @Override
    public void deleteOneDepartment(Long departmentId) {

    }

    /**
     public Department updateDepartment(Long departmentId, Department department) {
     Department depDB = departmentRepository.findById(departmentId).get();

     if(Objects.nonNull(department.getDepartmentName()) &&
     !"".equalsIgnoreCase(department.getDepartmentName())) {
     depDB.setDepartmentName(department.getDepartmentName());
     }

     if(Objects.nonNull(department.getDepartmentCode()) &&
     !"".equalsIgnoreCase(department.getDepartmentCode())) {
     depDB.setDepartmentCode(department.getDepartmentCode());
     }

     if(Objects.nonNull(department.getDepartmentAddress()) &&
     !"".equalsIgnoreCase(department.getDepartmentAddress())) {
     depDB.setDepartmentAddress(department.getDepartmentAddress());
     }

     return departmentRepository.save(depDB);
     }
     ======
     public Department fetchDepartmentById(Long departmentId) throws DepartmentNotFoundException {
     Optional<Department> department =
     departmentRepository.findById(departmentId);

     if(!department.isPresent()) {
     throw new DepartmentNotFoundException("Department Not Available");
     }

     return  department.get();
     }
     * */

    private Region getRegion(Region region){
        if(region.getRegionId () != null){
            return regionRepository.findById (region.getRegionId ( )).orElseThrow (
                    () -> new ResourceNotFoundException ("")
            );
        }
        return null;
    }
}
