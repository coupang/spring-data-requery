package org.springframework.data.requery.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SnappyCodecV2;
import org.redisson.config.Config;

/**
 * AbstractRedisCacheTest
 *
 * @author debop
 * @since 19. 3. 11
 */
@Slf4j
public abstract class AbstractRedisCacheTest {

    private static RedisContainer redis = new RedisContainer();

    public static Config getConfig() {
        Config config = new Config();

        config.useSingleServer()
            .setAddress(redis.getUrl())
            .setRetryAttempts(3)
            .setRetryInterval(100)
            .setConnectionMinimumIdleSize(2)
            .setConnectionPoolSize(16);

        config.setCodec(new SnappyCodecV2());

        return config;
    }

    public static RedissonClient getRedissonClient() {
        return Redisson.create(getConfig());
    }
}
