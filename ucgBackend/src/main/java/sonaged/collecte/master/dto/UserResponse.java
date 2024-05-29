package sonaged.collecte.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonaged.collecte.master.model.AuthorityEntity;

import java.time.Instant;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
public class UserResponse implements Serializable{
    Long userId;

    String userFirstname;

    String userLastname;

    String userEmail;

    String userCode;

    String userAddress;

    String userPhone;

    AuthorityEntity authority;

    Instant createAt;

    Instant updateAt;
}
