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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import springboot.apikey.example.util.UUIDUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class ApiKeyAuthManager implements AuthenticationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ApiKeyAuthManager.class);

    private final LoadingCache<String, Boolean> keys;

    public ApiKeyAuthManager(DataSource dataSource) {
        this.keys = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new DatabaseCacheLoader(dataSource));
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String principal = (String) authentication.getPrincipal();

        if (!keys.get(principal)) {
            throw new BadCredentialsException("The API key was not found or not the expected value.");
        } else {
            authentication.setAuthenticated(true);
            return authentication;
        }
    }

    /**
     * Caffeine CacheLoader that checks the database for the api key if it not found in the cache.
     */
    private static class DatabaseCacheLoader implements CacheLoader<String, Boolean> {
        private final DataSource dataSource;

        DatabaseCacheLoader(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Boolean load(String key) throws Exception {
            LOG.info("Loading api key from database: [key: {}]", key);

            try (Connection conn = dataSource.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM auth WHERE api_key = ?")) {
                    ps.setObject(1, UUIDUtil.fromHex(key));

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("An error occurred while retrieving api key from database", e);
                return false;
            }
        }
    }
}
