package sonaged.collecte.master.enums;

import java.text.MessageFormat;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum Permission {


    ACCESS_MY_USER ("ACCESS_MY_USER"),
    ACCESS_ALL_USERS ("ACCESS_ALL_USERS"),
    ACCESS_CONTROLS ("ACCESS_CONTROLS"),
    ACCESS_MY_ACCOUNT ("ACCESS_MY_ACCOUNT"),
    ACCESS_MY_ACTIVITIES ("ACCESS_MY_ACTIVITIES"),
    ACCESS_MY_ADDRESS ("ACCESS_MY_ADDRESS"),
    ACCESS_MY_CONSENT ("ACCESS_MY_CONSENT"),
    ACCESS_MY_CONTRACTS ("ACCESS_MY_CONTRACTS"),
    ACCESS_MY_SUBSCRIPTIONS ("ACCESS_MY_SUBSCRIPTIONS"),

    ACCESS_PRODUCT ("ACCESS_PRODUCT"),
    ACCESS_TERMINAL_INFO ("ACCESS_TERMINAL_INFO"),
    DISTRIBUTE_PRODUCT ("DISTRIBUTE_PRODUCT"),
    LOGIN_CONTROLER ("LOGIN_CONTROLER"),
    VALIDATE_ADDRESSES ("VALIDATE_ADDRESSES"),
    VALIDATE_IDENTITY ("VALIDATE_IDENTITY"),
    VALIDATE_PAYMENT_MEAN ("VALIDATE_PAYMENT_MEAN"),
    ACCESS_STATISTICS ("ACCESS_STATISTICS"),
    ACCESS_TOPOLOGY ("ACCESS_TOPOLOGY"),
    MANAGE_VALIDATION_REQUEST ("MANAGE_VALIDATION_REQUEST"),
    ACCESS_ALL_EVENTS ("ACCESS_ALL_EVENTS"),
    ACCESS_MY_EVENTS ("ACCESS_MY_EVENTS"),
    CONFIGURE_MY_USER("CONFIGURE_MY_USER"),
    MANAGE_ALARM("MANAGE_ALARM"),
    MANAGE_STATISTICS("MANAGE_STATISTICS"),
    REMOVE_ACCOUNT("REMOVE_ACCOUNT"),
    ACCESS_MONITORING_MYCOMPANY_TERMINAL("ACCESS_MONITORING_MYCOMPANY_TERMINAL"),
    ACCESS_MYCOMPANY_STATISTICS("ACCESS_MYCOMPANY_STATISTICS"),
    ACCESS_MYCOMPANY_USERS("ACCESS_MYCOMPANY_USERS"),
    ACCESS_STATISTICS_MYCOMPANY_TERMINAL("ACCESS_STATISTICS_MYCOMPANY_TERMINAL"),
    MANAGE_ROLES ("MANAGE_ROLES"),
    ACCESS_RULES_PARAMETERS ("ACCESS_RULES_PARAMETERS");


    private String value;

    Permission(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Permission fromValue(String value) {
        for (Permission permission : values()) {
            if (permission.value.equalsIgnoreCase(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException(MessageFormat.format("{0} not found with the value: {1}", Permission.class.getSimpleName(), value));
    }

}
