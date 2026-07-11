package sonaged.collecte.master.security.rules;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import sonaged.collecte.master.enums.Permission;
import sonaged.collecte.master.security.SecurityRule;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorityRules {

    static final String ROLE_API_PREFIX = "/v1/authorities";
    static final String ROLE_NAME_API_PREFIX = "/{authorityName}";
    static final String ROLE_ID_API_PREFIX = "/{authorityId}";

    static final String ROLE_PERMISSION_API_PREFIX = ROLE_API_PREFIX + ROLE_NAME_API_PREFIX + "/permissions" ;

    @Bean
    public SecurityRule createRole() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.POST)
                .apiPattern(ROLE_API_PREFIX)
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }

    @Bean
    public SecurityRule readRoleByRoleName() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.GET)
                .apiPattern( ROLE_API_PREFIX +"/_name"+ ROLE_NAME_API_PREFIX)
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }

    @Bean
    public SecurityRule readRoles() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.GET)
                .apiPattern(ROLE_API_PREFIX)
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }

    @Bean
    public SecurityRule updateRole() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.PUT)
                .apiPattern(ROLE_API_PREFIX + ROLE_ID_API_PREFIX)
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }

    @Bean
    public SecurityRule deleteRole() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.DELETE)
                .apiPattern(ROLE_API_PREFIX + ROLE_ID_API_PREFIX)
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }

    @Bean
    public SecurityRule getRolePermissions() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.GET)
                .apiPattern(ROLE_PERMISSION_API_PREFIX)
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }
    
    @Bean
    public SecurityRule addPermission() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.PUT)
                .apiPattern(ROLE_PERMISSION_API_PREFIX + "/{permission}")
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }
    @Bean
    public SecurityRule deletePermission() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.DELETE)
                .apiPattern(ROLE_PERMISSION_API_PREFIX + "/{permission}")
                .build()
                .condition()
                    .hasPermission(Permission.MANAGE_ROLE.toString())
                .end();
    }

}
