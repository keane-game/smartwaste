package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
