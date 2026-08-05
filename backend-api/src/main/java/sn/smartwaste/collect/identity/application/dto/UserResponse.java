package sn.smartwaste.collect.identity.application.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;

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
    UUID userId;

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
