package sonaged.collecte.master.security;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

public class SecurityRule {
    private static final Logger log = LoggerFactory.getLogger(SecurityRule.class);
    private @NotNull HttpMethod httpMethod;
    private @NotEmpty List<String> apiPatterns;
    private boolean authenticated;
    private List<Condition> conditions;

    public void configure(HttpSecurity httpSecurity, ApplicationContext applicationContext) throws Exception {
        String authenticationAccess = this.authenticated ? "isAuthenticated()" : null;
        String allConditions = ((StringJoiner)this.conditions.stream().map(Condition::toFormat).filter((s) -> {
            return s != null && !s.isEmpty();
        }).collect(() -> {
            return (new StringJoiner(" or ")).setEmptyValue("");
        }, StringJoiner::add, StringJoiner::merge)).toString();
        String access = ((StringJoiner)Arrays.stream(new String[]{authenticationAccess, allConditions}).filter((s) -> {
            return s != null && !s.isEmpty();
        }).collect(() -> {
            return (new StringJoiner(" and ")).setEmptyValue("");
        }, StringJoiner::add, StringJoiner::merge)).toString();
        if (access.isEmpty()) {
            log.trace("Configuring access for URIs {} \"{}\" with no condition", this.httpMethod, this.apiPatterns);
            httpSecurity.authorizeHttpRequests((requests) -> {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)requests.requestMatchers(this.httpMethod, (String[])this.apiPatterns.toArray(new String[0]))).permitAll();
            });
        } else {
            log.trace("Configuring access for URIs {} \"{}\" with this condition : {}", new Object[]{this.httpMethod, this.apiPatterns, access});
            httpSecurity.authorizeHttpRequests((requests) -> {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)requests.requestMatchers(this.httpMethod, (String[])this.apiPatterns.toArray(new String[0]))).access(this.getWebExpressionAuthorizationManager(access, applicationContext));
            });
        }

    }

    private WebExpressionAuthorizationManager getWebExpressionAuthorizationManager(String access, ApplicationContext applicationContext) {
        DefaultHttpSecurityExpressionHandler expressionHandler = new DefaultHttpSecurityExpressionHandler();
        expressionHandler.setApplicationContext(applicationContext);
        WebExpressionAuthorizationManager authorizationManager = new WebExpressionAuthorizationManager(access);
        authorizationManager.setExpressionHandler(expressionHandler);
        return authorizationManager;
    }

    public Condition condition() {
        Condition condition = new Condition(this, this);
        this.conditions.add(condition);
        return condition;
    }

    private static boolean $default$authenticated() {
        return true;
    }

    private static List<Condition> $default$conditions() {
        return new ArrayList();
    }

    SecurityRule(final HttpMethod httpMethod, final List<String> apiPatterns, final boolean authenticated, final List<Condition> conditions) {
        this.httpMethod = httpMethod;
        this.apiPatterns = apiPatterns;
        this.authenticated = authenticated;
        this.conditions = conditions;
    }

    public static SecurityRuleBuilder builder() {
        return new SecurityRuleBuilder();
    }

    public class Condition {
        private Set<String> permissions = new HashSet();
        private String controlMethod;
        private final SecurityRule securityRule;

        public Condition hasPermission(String permission) {
            this.permissions.add(permission);
            return this;
        }

        public Condition controlMethod(String controlMethod) {
            this.controlMethod = controlMethod;
            return this;
        }

        public Condition or() {
            return this.securityRule.condition();
        }

        public SecurityRule end() {
            return this.securityRule;
        }

        private String toFormat() {
            StringJoiner result = (new StringJoiner(" and ")).setEmptyValue("");
            if (!this.permissions.isEmpty()) {
                this.permissions = (Set)this.permissions.stream().map((permission) -> {
                    return "'" + permission + "'";
                }).collect(Collectors.toSet());
                String anyAuthorities = String.join(",", this.permissions);
                result.add("hasAnyAuthority(" + anyAuthorities + ")");
            }

            if (!StringUtils.isEmpty(this.controlMethod)) {
                result.add(this.controlMethod);
            }

            return result.toString();
        }

        public Condition(final SecurityRule this$0, final SecurityRule securityRule) {
            this.securityRule = securityRule;
        }
    }

    public static class SecurityRuleBuilder {
        private HttpMethod httpMethod;
        private ArrayList<String> apiPatterns;
        private boolean authenticated$set;
        private boolean authenticated$value;
        private boolean conditions$set;
        private List<Condition> conditions$value;

        SecurityRuleBuilder() {
        }

        public SecurityRuleBuilder httpMethod(final HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public SecurityRuleBuilder apiPattern(final String apiPattern) {
            if (this.apiPatterns == null) {
                this.apiPatterns = new ArrayList();
            }

            this.apiPatterns.add(apiPattern);
            return this;
        }

        public SecurityRuleBuilder apiPatterns(final Collection<? extends String> apiPatterns) {
            if (apiPatterns == null) {
                throw new NullPointerException("apiPatterns cannot be null");
            } else {
                if (this.apiPatterns == null) {
                    this.apiPatterns = new ArrayList();
                }

                this.apiPatterns.addAll(apiPatterns);
                return this;
            }
        }

        public SecurityRuleBuilder clearApiPatterns() {
            if (this.apiPatterns != null) {
                this.apiPatterns.clear();
            }

            return this;
        }

        public SecurityRuleBuilder authenticated(final boolean authenticated) {
            this.authenticated$value = authenticated;
            this.authenticated$set = true;
            return this;
        }

        public SecurityRuleBuilder conditions(final List<Condition> conditions) {
            this.conditions$value = conditions;
            this.conditions$set = true;
            return this;
        }

        public SecurityRule build() {
            List apiPatterns;
            switch (this.apiPatterns == null ? 0 : this.apiPatterns.size()) {
                case 0:
                    apiPatterns = Collections.emptyList();
                    break;
                case 1:
                    apiPatterns = Collections.singletonList((String)this.apiPatterns.get(0));
                    break;
                default:
                    apiPatterns = Collections.unmodifiableList(new ArrayList(this.apiPatterns));
            }

            boolean authenticated$value = this.authenticated$value;
            if (!this.authenticated$set) {
                authenticated$value = SecurityRule.$default$authenticated();
            }

            List<Condition> conditions$value = this.conditions$value;
            if (!this.conditions$set) {
                conditions$value = SecurityRule.$default$conditions();
            }

            return new SecurityRule(this.httpMethod, apiPatterns, authenticated$value, conditions$value);
        }

        public String toString() {
            String var10000 = String.valueOf(this.httpMethod);
            return "SecurityRule.SecurityRuleBuilder(httpMethod=" + var10000 + ", apiPatterns=" + String.valueOf(this.apiPatterns) + ", authenticated$value=" + this.authenticated$value + ", conditions$value=" + String.valueOf(this.conditions$value) + ")";
        }
    }
}



