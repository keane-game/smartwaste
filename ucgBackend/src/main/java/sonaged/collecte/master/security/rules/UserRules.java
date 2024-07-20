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
public class UserRules {

    static final String CLIENTS_API_PREFIX = "/clients";

    static final String REALMS_API_PREFIX = "/realms";

    static final String USERS_API_PREFIX = "/v1/users";

    static final String USER_ID = "/{userId}";


    @Bean
    public SecurityRule createUser() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.POST)
                .apiPattern(USERS_API_PREFIX)
                .build()
                .condition()
                .hasPermission(Permission.ACCESS_ADMIN.toString())
                .or()
                .hasPermission(Permission.USER_VIEW.toString())
                .end();
    }

    @Bean
    public SecurityRule readUsers() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.GET)
                .apiPattern(USERS_API_PREFIX)
                .build()
                .condition()
                .hasPermission(Permission.ACCESS_ADMIN.toString())
                .end();
    }

    @Bean
    public SecurityRule readUserById() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.GET)
                .apiPattern(USERS_API_PREFIX + USER_ID)
                .build()
                .condition()
                .hasPermission(Permission.ACCESS_ADMIN.toString())
                .or()
                .hasPermission(Permission.USER_VIEW.toString())
                .end();
    }


    @Bean
    public SecurityRule updateUserById() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.PUT)
                .apiPattern(USERS_API_PREFIX + USER_ID)
                .build()
                .condition()
                .hasPermission(Permission.ACCESS_ADMIN.toString())
                .or()
                .hasPermission(Permission.USER_VIEW.toString())
                .end();
    }

    @Bean
    public SecurityRule deleteUserById() {
        return SecurityRule.builder()
                .httpMethod(HttpMethod.DELETE)
                .apiPattern(USERS_API_PREFIX + USER_ID)
                .build()
                .condition()
                .hasPermission(Permission.ACCESS_ADMIN.toString())
                .or()
                .hasPermission(Permission.USER_VIEW.toString())
                .end();
    }



}
