package ucg.collecte.master.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UCG_DEPARTMENT")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "departmentId")
    private  Long departmentId;

    @Column(name = "departmentName")
    private String departmentName;

    @Column(name = "departmentCode")
    private String departmentCode;
}
