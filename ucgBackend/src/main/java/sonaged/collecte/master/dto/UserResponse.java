package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.Authority;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private  Long userId;

    private String userFirstname;

    private String userLastname;

    private String userEmail;

    private String userCode;

    private String userAddress;

    private String userPhone;

    private Authority authority;

    private Instant createAt;

    private Instant updateAt;
}
