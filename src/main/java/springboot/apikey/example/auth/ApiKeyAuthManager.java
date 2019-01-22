package springboot.apikey.example.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ApiKeyAuthManager implements AuthenticationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ApiKeyAuthManager.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String principal = (String) authentication.getPrincipal();

        if (!"test".equals(principal))
        {
            throw new BadCredentialsException("The API key was not found or not the expected value.");
        }

        authentication.setAuthenticated(true);
        return authentication;
    }
}
