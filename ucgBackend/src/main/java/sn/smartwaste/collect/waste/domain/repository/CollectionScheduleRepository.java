package sn.smartwaste.collect.waste.domain.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.smartwaste.collect.waste.domain.model.CollectionSchedule;

@Repository
public interface CollectionScheduleRepository extends JpaRepository<CollectionSchedule, UUID> {

    List<CollectionSchedule> findByDayOfWeekAndActiveTrue(DayOfWeek dayOfWeek);

    List<CollectionSchedule> findByQuartierIdAndActiveTrue(UUID quartierId);
}
