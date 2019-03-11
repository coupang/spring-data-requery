/*
 * Copyright 2018 Coupang Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.requery.cache.cache2k;

import io.requery.EntityCache;
import io.requery.cache.EntityCacheBuilder;
import io.requery.meta.EntityModel;
import org.cache2k.jcache.provider.JCacheProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;
import org.springframework.data.requery.domain.basic.BasicUser;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EntityCache with Cache2k-JCache
 *
 * @author debop
 * @since 18. 6. 5
 */
public class EntityCacheWithCache2kJCacheTest extends AbstractDomainTest {

    @Inject
    EntityModel entityModel;

    private EntityCache cache;

    private EntityCache getCache(EntityModel entityModel) {
        CachingProvider provider = Caching.getCachingProvider(JCacheProvider.class.getName());
        CacheManager cacheManager = provider.getCacheManager();

        return new EntityCacheBuilder(entityModel)
            .useReferenceCache(false)
            .useSerializableCache(true)
            .useCacheManager(cacheManager)
            .build();
    }

    @Before
    public void setup() {
        if (cache == null) {
            cache = getCache(entityModel);
        }
    }

    @Test
    public void serializeEntityToCache() {
        BasicUser user = new BasicUser();
        int id = 100;

        cache.put(BasicUser.class, id, user);

        BasicUser loaded = cache.get(BasicUser.class, id);

        assertThat(loaded).isNotNull();
        assertThat(loaded).isEqualTo(user);
    }
}
