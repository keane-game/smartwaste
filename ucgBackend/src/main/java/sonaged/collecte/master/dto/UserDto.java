package sonaged.collecte.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Authority;

import java.time.Instant;
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
