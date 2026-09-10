/**
 * Copyright 2019 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package springboot.apikey.example.auth;

import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;

import static org.junit.Assert.fail;

public class ApiKeyAuthManagerTest {
    private final DataSource untouchedDataSource = (DataSource) Proxy.newProxyInstance(
            DataSource.class.getClassLoader(),
            new Class<?>[]{DataSource.class},
            (proxy, method, args) -> {
                throw new AssertionError("The database must not be touched for a rejected principal");
            });

    @Test
    public void rejectsNullAuthenticationBeforeDatabaseAccess() {
        assertRejected(null);
    }

    @Test
    public void rejectsNullPrincipalBeforeDatabaseAccess() {
        assertRejected(authenticationWithPrincipal(null));
    }

    @Test
    public void rejectsNonStringPrincipalBeforeDatabaseAccess() {
        assertRejected(authenticationWithPrincipal(new Object()));
    }

    @Test
    public void rejectsEmptyApiKeyBeforeDatabaseAccess() {
        assertRejected(authenticationWithPrincipal(""));
    }

    @Test
    public void rejectsWhitespaceOnlyApiKeyBeforeDatabaseAccess() {
        assertRejected(authenticationWithPrincipal(" \t\n"));
    }

    @Test
    public void rejects31CharacterApiKeyBeforeDatabaseAccess() {
        assertRejected(authenticationWithPrincipal("0123456789abcdef0123456789abcde"));
    }

    @Test
    public void rejects33CharacterApiKeyBeforeDatabaseAccess() {
        assertRejected(authenticationWithPrincipal("0123456789abcdef0123456789abcdef0"));
    }

    @Test
    public void rejectsNonHexApiKeyBeforeDatabaseAccess() {
        assertRejected(authenticationWithPrincipal("0123456789abcdef0123456789abcdeg"));
    }

    private void assertRejected(Authentication authentication) {
        ApiKeyAuthManager manager = new ApiKeyAuthManager(untouchedDataSource);

        try {
            manager.authenticate(authentication);
            fail("Expected BadCredentialsException");
        } catch (BadCredentialsException expected) {
            // Expected.
        }
    }

    private Authentication authenticationWithPrincipal(Object principal) {
        return (Authentication) Proxy.newProxyInstance(
                Authentication.class.getClassLoader(),
                new Class<?>[]{Authentication.class},
                (proxy, method, args) -> {
                    if ("getPrincipal".equals(method.getName())) {
                        return principal;
                    }
                    throw new AssertionError("Unexpected Authentication method: " + method.getName());
                });
    }
}
