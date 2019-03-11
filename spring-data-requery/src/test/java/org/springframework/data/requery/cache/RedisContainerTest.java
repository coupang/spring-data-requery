package org.springframework.data.requery.cache;

import org.junit.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RedisContainerTest
 *
 * @author debop
 * @since 19. 3. 11
 */
public class RedisContainerTest extends AbstractRedisCacheTest {

    @Test
    public void run_embedded_redis_server() {

        RedissonClient redisson = getRedissonClient();

        RMap<String, String> map = redisson.getMap("map");

        map.fastPut("key1", "value1");

        RMap<String, String> map2 = redisson.getMap("map");

        assertThat(map2.get("key1")).isEqualTo("value1");
    }

}
