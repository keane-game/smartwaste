package sonaged.collecte.master.model;

import sonaged.collecte.master.enums.Permission;

import java.time.Instant;
import java.util.Collection;

/**
 * Projection for {@link Authority}
 */
public interface AuthorityInfo {
    String getCreatedBy();

    Instant getCreatedDate();

    String getLastModifiedBy();

    Instant getLastModifiedDate();

    Long getAuthorityId();

    String getAuthorityName();

    String getAuthorityRealm();

    String getAuthorityDescription();

    Collection<Permission> getPermissions();
}