package ucg.collecte.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucg.collecte.master.model.Geolocation;

@Repository
public interface GeolocationRepository extends JpaRepository<Geolocation, Long> {
}
