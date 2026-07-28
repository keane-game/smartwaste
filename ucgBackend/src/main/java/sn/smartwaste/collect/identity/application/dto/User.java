package sn.smartwaste.collect.identity.application.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User  implements Serializable{

    UUID userId;

    String userFirstname;

    String userLastname;

    String userEmail;

    String userCode;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String userPassword;

    String userAddress;

    String userPhone;
    boolean activated;

    AuthorityEntity authority;
}
