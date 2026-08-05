package sn.smartwaste.collect.identity.domain.model;

import java.util.UUID;

import sn.smartwaste.collect.identity.domain.model.Permission;

import java.time.Instant;
import java.util.Collection;

/**
 * Projection for {@link AuthorityEntity}
 */
public interface AuthorityInfo {
    String getCreatedBy();

    Instant getCreatedDate();

    String getLastModifiedBy();

    Instant getLastModifiedDate();

    UUID getAuthorityId();

    String getAuthorityName();

    String getAuthorityRealm();

    String getAuthorityDescription();

    Collection<Permission> getPermissions();
}