package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String userFirstname;

    private String userLastname;

    private String userEmail;

    private String userCode;

    private String password;

    private String userAddress;

    private String userPhone;

    private Authority authority;

    private Instant createAt;

    private Instant updateAt;
}
