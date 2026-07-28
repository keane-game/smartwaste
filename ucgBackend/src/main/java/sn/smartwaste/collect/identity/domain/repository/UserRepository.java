package sn.smartwaste.collect.identity.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;
import sn.smartwaste.collect.identity.domain.model.UserEntity;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findByAuthority(AuthorityEntity authority);
    Optional<UserEntity> findByUserEmail(String email);
    //User findByUserEmail(String email);
}
