package ch.admin.bag.covidcertificate.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * When including the jeap-spring-boot-security-starter dependency and providing the matching configuration properties
 * all web endpoints of the application will be automatically protected by OAuth2 as a default. If in addition web endpoints
 * with different protection (i.e. basic auth or no protection at all) must be provided at the same time by the application
 * an additional WebSecurityConfigurerAdapter configuration (like the one below) needs to explicitly punch a hole into
 * the jeap-spring-boot-security-starter OAuth2 protection with an appropriate HttpSecurity configuration.
 * Note: jeap-spring-boot-monitoring-starter already does exactly that for the prometheus actuator endpoint.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestInternalSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ADMIN_ROLE = "ADMIN";

    @Value("${cc-printing-service.internal.maintenance.user}")
    private String user;
    @Value("${cc-printing-service.internal.maintenance.password}")
    private String password;

    @Autowired
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(user)
                .password(password)
                .authorities(ADMIN_ROLE);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatcher(new AntPathRequestMatcher("/api/int/**"))
                .authorizeRequests()
                .antMatchers("/api/int/**")
                .fullyAuthenticated()
                .and()
                .httpBasic();
    }
}