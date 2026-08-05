package sn.smartwaste.collect.waste.domain.model;

import sn.smartwaste.collect.shared.domain.model.AbstractAuditingEntity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "HISTORY")
public class HistoryEntity extends AbstractAuditingEntity<Long> {

    @Id
    private  Long id;

}
