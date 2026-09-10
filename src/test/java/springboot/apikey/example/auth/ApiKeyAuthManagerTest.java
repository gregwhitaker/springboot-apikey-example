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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
    public void authenticatesValidHexApiKeyAndReusesCachedDatabaseLookup() {
        String apiKey = "00112233445566778899aabbccddeeff";
        UUID expectedUuid = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");
        int[] databaseLookups = {0};
        Object[] jdbcParameter = {null};
        boolean[] connectionClosed = {false};
        boolean[] statementClosed = {false};
        boolean[] resultSetClosed = {false};

        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    if ("next".equals(method.getName())) {
                        return true;
                    }
                    if ("close".equals(method.getName())) {
                        resultSetClosed[0] = true;
                        return null;
                    }
                    throw new AssertionError("Unexpected ResultSet method: " + method.getName());
                });
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("setObject".equals(method.getName())) {
                        assertEquals(1, args[0]);
                        jdbcParameter[0] = args[1];
                        return null;
                    }
                    if ("executeQuery".equals(method.getName())) {
                        return resultSet;
                    }
                    if ("close".equals(method.getName())) {
                        statementClosed[0] = true;
                        return null;
                    }
                    throw new AssertionError("Unexpected PreparedStatement method: " + method.getName());
                });
        Connection connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        assertEquals("SELECT * FROM auth WHERE api_key = ?", args[0]);
                        return statement;
                    }
                    if ("close".equals(method.getName())) {
                        connectionClosed[0] = true;
                        return null;
                    }
                    throw new AssertionError("Unexpected Connection method: " + method.getName());
                });
        DataSource dataSource = (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        databaseLookups[0]++;
                        return connection;
                    }
                    throw new AssertionError("Unexpected DataSource method: " + method.getName());
                });
        boolean[] authenticated = {false};
        Authentication authentication = (Authentication) Proxy.newProxyInstance(
                Authentication.class.getClassLoader(),
                new Class<?>[]{Authentication.class},
                (proxy, method, args) -> {
                    if ("getPrincipal".equals(method.getName())) {
                        return apiKey;
                    }
                    if ("setAuthenticated".equals(method.getName())) {
                        authenticated[0] = (Boolean) args[0];
                        return null;
                    }
                    if ("isAuthenticated".equals(method.getName())) {
                        return authenticated[0];
                    }
                    throw new AssertionError("Unexpected Authentication method: " + method.getName());
                });

        ApiKeyAuthManager manager = new ApiKeyAuthManager(dataSource);

        assertSame(authentication, manager.authenticate(authentication));
        assertSame(authentication, manager.authenticate(authentication));

        assertTrue(authentication.isAuthenticated());
        assertEquals(expectedUuid, jdbcParameter[0]);
        assertEquals(1, databaseLookups[0]);
        assertTrue(resultSetClosed[0]);
        assertTrue(statementClosed[0]);
        assertTrue(connectionClosed[0]);
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
