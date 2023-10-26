package ucg.collecte.master.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="`USER`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long userId;

    @Column
    private String userName;

    @Column
    private String userEmail;

    @Column
    private String userCode;

    @Column
    private String userAddress;

    @Column
    private String userPhone;

    @ManyToMany
    private Set<Role> roles;

}
