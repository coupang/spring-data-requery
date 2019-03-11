package org.springframework.data.requery.cache.redis;

import io.requery.meta.EntityModel;
import io.requery.meta.Type;
import io.requery.util.ClassMap;
import lombok.extern.slf4j.Slf4j;

/**
 * RedisSerializationContext
 *
 * @author debop
 * @since 19. 3. 11
 */
@Slf4j
class RedisSerializationContext {

    private RedisSerializationContext() {}

    private static final ClassMap<Type<?>> types = new ClassMap<>();

    public static void map(EntityModel model) {
        log.info("All entity type is mapped. model name={}", model.getName());
        for (Type<?> type : model.getTypes()) {
            types.put(type.getClassType(), type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> Type<E> getType(Class<E> entityClass) {
        Type<E> type = (Type<E>) types.get(entityClass);
        if (type == null) {
            throw new IllegalStateException("Not found entity class. " + entityClass.getName());
        }
        return type;
    }
}
