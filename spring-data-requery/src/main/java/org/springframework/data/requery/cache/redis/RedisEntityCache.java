package org.springframework.data.requery.cache.redis;

import io.requery.EntityCache;
import io.requery.meta.Attribute;
import io.requery.meta.EntityModel;
import io.requery.meta.Type;
import io.requery.proxy.CompositeKey;
import io.requery.util.ClassMap;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Redis 를 분산캐시로 사용하는 {@link EntityCache}
 *
 * @author debop
 * @since 19. 3. 11
 */
@Slf4j
public class RedisEntityCache implements EntityCache {

    private final EntityModel model;
    private final RedissonClient redisson;
    private final ClassMap<RMapCache<Object, RedisSerializedEntity<?>>> caches = new ClassMap<>();
    private final Object syncObj = new Object();

    public RedisEntityCache(@Nonnull final EntityModel model, @Nonnull final RedissonClient redisson) {
        this.model = model;
        this.redisson = redisson;

        RedisSerializationContext.map(model);
    }

    private String getCacheName(Type<?> type) {
        return type.getName();
    }

    private <T> Class getKeyClass(Type<T> type) {
        Set<Attribute<T, ?>> keys = type.getKeyAttributes();
        Class keyClass;
        if (keys.isEmpty()) {
            // use hash code
            return Integer.class;
        }
        if (keys.size() == 1) {
            Attribute<?, ?> attribute = keys.iterator().next();
            if (attribute.isAssociation()) {
                attribute = attribute.getReferencedAttribute().get();
            }
            keyClass = attribute.getClassType();
            if (keyClass.isPrimitive()) {
                if (keyClass == int.class) {
                    keyClass = Integer.class;
                } else if (keyClass == long.class) {
                    keyClass = Long.class;
                }
            }
        } else {
            keyClass = CompositeKey.class;
        }
        return keyClass;
    }

    private RMapCache getCache(Class<?> type) {
        return caches.computeIfAbsent(type, (key) -> {
            Type declaredType = model.typeOf(key);
            String cacheName = getCacheName(declaredType);

            return redisson.getMapCache(cacheName);
        });
    }

    @Override
    public <T> T get(Class<T> type, Object key) {
        log.trace("Load cache entity from redis. type={}, key={}", type, key);
        RMapCache cache = getCache(type);

        RedisSerializedEntity container = (RedisSerializedEntity) cache.get(key);
        return (container != null) ? type.cast(container.getEntity()) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void put(Class<T> type, Object key, T value) {
        log.trace("Save cache entity to redis. type={}, key={}, value={}", type, key, value);
        RMapCache<Object, RedisSerializedEntity<T>> cache = getCache(type);
        cache.put(key, new RedisSerializedEntity<>(type, value));
    }

    @Override
    public boolean contains(Class<?> type, Object key) {
        return getCache(type).containsKey(key);
    }

    @Override
    public void invalidate(Class<?> type) {
        log.trace("Invalidate all cache entity. type={}", type);

        RMapCache cache = getCache(type);
        if (cache != null) {
            cache.clear();
            String cacheName = getCacheName(model.typeOf(type));
            synchronized (syncObj) {
                caches.remove(type);
            }
        }
    }

    @Override
    public void invalidate(Class<?> type, Object key) {
        getCache(type).remove(key);
    }

    @Override
    public void clear() {
        log.info("Clear all cache entities");

        synchronized (syncObj) {
            caches.forEach((key, cache) -> invalidate(key));
            caches.clear();
        }
    }
}
