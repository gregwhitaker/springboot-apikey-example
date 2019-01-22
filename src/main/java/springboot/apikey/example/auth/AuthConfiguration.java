package springboot.apikey.example.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@Order(1)
public class AuthConfiguration extends WebSecurityConfigurerAdapter {
    private static final String API_KEY_AUTH_HEADER_NAME = "API_KEY";

    @Autowired
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/api/**").
                csrf().
                disable().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and()
                    .addFilter(apiKeyAuthFilter)
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated();
    }

    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter(AuthenticationManager authenticationManager) {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(API_KEY_AUTH_HEADER_NAME);
        filter.setAuthenticationManager(authenticationManager);

        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ApiKeyAuthManager();
    }
}
