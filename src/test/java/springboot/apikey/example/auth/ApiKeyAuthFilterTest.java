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

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApiKeyAuthFilterTest {

    @Test
    public void rejectsNullHeaderName() {
        assertInvalidHeaderName(null);
    }

    @Test
    public void rejectsEmptyHeaderName() {
        assertInvalidHeaderName("");
    }

    @Test
    public void rejectsWhitespaceOnlyHeaderName() {
        assertInvalidHeaderName(" \t\n");
    }

    @Test
    public void preservesCustomHeaderNameExactly() {
        final String headerName = " X-Custom-Api-Key ";
        final ApiKeyAuthFilter filter = new ApiKeyAuthFilter(headerName);
        final HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName())) {
                        return headerName.equals(args[0]) ? "secret" : null;
                    }
                    return null;
                });

        assertEquals("secret", filter.getPreAuthenticatedPrincipal(request));
    }

    private void assertInvalidHeaderName(final String headerName) {
        try {
            new ApiKeyAuthFilter(headerName);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("header name"));
        }
    }
}
