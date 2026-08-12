package sn.smartwaste.collect.identity.application.dto;

import java.util.Collection;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import sn.smartwaste.collect.identity.domain.model.Permission;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Authority implements Serializable {

    UUID authorityId;

    String name;

    String description;

    Collection<Permission> permissions;
}
