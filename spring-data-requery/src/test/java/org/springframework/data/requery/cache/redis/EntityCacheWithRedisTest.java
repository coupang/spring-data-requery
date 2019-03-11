package org.springframework.data.requery.cache.redis;

import io.requery.EntityCache;
import io.requery.meta.EntityModel;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.cache.AbstractRedisCacheTest;
import org.springframework.data.requery.domain.Models;
import org.springframework.data.requery.domain.basic.BasicUser;

import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * EntityCacheWithRedisTest
 *
 * @author debop
 * @since 19. 3. 11
 */
public class EntityCacheWithRedisTest extends AbstractRedisCacheTest {

    private final EntityModel entityModel = Models.DEFAULT;

    private EntityCache cache;

    private EntityCache getEntityCache(EntityModel model) {
        return new RedisEntityCache(model, getRedissonClient());
    }

    @Before
    public void setup() {
        if (cache == null) {
            cache = getEntityCache(entityModel);
        }
    }

    @Test
    public void save_cache() {
        BasicUser user = new BasicUser();
        user.setUuid(UUID.randomUUID());

        int id = 100;

        cache.put(BasicUser.class, id, user);

        BasicUser cached = cache.get(BasicUser.class, id);

        assertThat(cached).isNotNull();
        assertThat(cached).isEqualTo(user);
    }
}
