package org.springframework.data.requery.cache.cache2k;

import io.requery.EntityCache;
import io.requery.sql.EntityDataStore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.configuration.Cache2kConfiguration;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache2kEntityCache
 *
 * @author debop
 * @since 19. 3. 11
 */
@SuppressWarnings("unchecked")
@Slf4j
public class Cache2kEntityCache implements EntityCache {

    private static final Cache2kConfiguration<Object, Object> defaultConfiguration;

    private static final Map<Class<?>, Cache<Object, Object>> cacheManagers = new ConcurrentHashMap<>();
    private static final Object syncObj = new Object();

    static {
        defaultConfiguration = Cache2kConfiguration.of(Object.class, Object.class);
        defaultConfiguration.setEntryCapacity(20000L);
        defaultConfiguration.setEternal(true);
        defaultConfiguration.setKeepDataAfterExpired(false);
        defaultConfiguration.setBoostConcurrency(true);
        defaultConfiguration.setRetryInterval(10);
        defaultConfiguration.setMaxRetryInterval(1000L);
    }

    private final Cache2kConfiguration<Object, Object> configuration;

    // For auto loading
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private EntityDataStore<Object> dataStore = null;

    public Cache2kEntityCache() {
        this(defaultConfiguration);
    }

    public Cache2kEntityCache(@Nonnull final Cache2kConfiguration<Object, Object> configuration) {
        this.configuration = configuration;
    }

    private Cache getCache(Class<?> entityType) {
        return cacheManagers.computeIfAbsent(entityType, (key) -> {
            log.debug("Create cache2k cache for type {}", key.getName());

            Cache2kBuilder<Object, Object> builder = Cache2kBuilder.of(configuration).name(key.getName());

            // For auto loading
            if (dataStore != null) {
                builder.loader((id) -> dataStore.findByKey(key, id));
            }
            return builder.build();
        });
    }

    @Override
    public <T> T get(Class<T> type, Object key) {
        return (T) getCache(type).get(key);
    }

    @Override
    public <T> void put(Class<T> type, Object key, T value) {
        if (value != null) {
            getCache(type).put(key, value);
        } else {
            invalidate(type, key);
        }
    }

    @Override
    public boolean contains(Class<?> type, Object key) {
        return getCache(type).containsKey(key);
    }

    @Override
    public void invalidate(Class<?> type) {
        getCache(type).removeAll();
    }

    @Override
    public void invalidate(Class<?> type, Object key) {
        getCache(type).remove(key);
    }

    @Override
    public void clear() {
        synchronized (syncObj) {
            cacheManagers.forEach((type, cache) -> cache.clearAndClose());
            cacheManagers.clear();
        }
    }
}
