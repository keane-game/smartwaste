package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sonaged.collecte.master.model.AuthorityEntity;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User  implements Serializable{

    Long userId;

    String userFirstname;

    String userLastname;

    String userEmail;

    String userCode;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String userPassword;

    String userAddress;

    String userPhone;

    AuthorityEntity authority;
}
