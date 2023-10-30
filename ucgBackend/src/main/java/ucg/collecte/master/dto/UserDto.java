package ucg.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ucg.collecte.master.model.Authority;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private  Long userId;

    private String userName;

    private String userEmail;

    private String userCode;

    private String userAddress;

    private String userPhone;

    private Set<Authority> authorities;

    private Instant createAt;

    private Instant updateAt;
}
