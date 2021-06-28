package ch.admin.bag.covidcertificate.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
    @EnableWebSecurity
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