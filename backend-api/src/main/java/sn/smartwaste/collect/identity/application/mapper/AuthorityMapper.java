package sn.smartwaste.collect.identity.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import sn.smartwaste.collect.identity.application.dto.Authority;
import sn.smartwaste.collect.identity.domain.model.AuthorityEntity;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "string")
public interface AuthorityMapper {

    AuthorityMapper AMP = Mappers.getMapper(AuthorityMapper.class);

    Authority asDto(AuthorityEntity authority);

    // Sens inverse : `users` (association inverse) n'a pas de pendant dans le DTO et ne doit
    // jamais être reconstruit depuis une requête cliente.
    @Mapping(target = "users", ignore = true)
    AuthorityEntity asModel(Authority authority);

    List<Authority> asListDto(List<AuthorityEntity> authorities);
}
