package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.Role;


@Repository
public interface RoleRepository  extends JpaRepository<Role, Long> {
}
