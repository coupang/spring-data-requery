package org.springframework.data.requery.cache.redis;

import io.requery.meta.Attribute;
import io.requery.meta.Type;
import io.requery.proxy.EntityProxy;
import io.requery.proxy.PropertyState;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Serialized Entity for caching to Redis Server.
 *
 * @author debop
 * @since 19. 3. 11
 */
@Slf4j
class RedisSerializedEntity<E> implements Serializable {

    private final Class<E> entityClass;
    private transient E entity;

    public RedisSerializedEntity(@Nonnull final Class<E> entityClass, final E entity) {
        this.entityClass = entityClass;
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }

    private void readObject(@Nonnull final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        Type<E> type = RedisSerializationContext.getType(entityClass);
        entity = type.getFactory().get();
        EntityProxy<E> proxy = type.getProxyProvider().apply(entity);

        for (Attribute<E, ?> attribute : type.getAttributes()) {
            // currently only non-associative properties are serialized
            if (attribute.isAssociation()) {
                proxy.setState(attribute, PropertyState.FETCH);
                continue;
            }
            Object value = stream.readObject();
            proxy.setObject(attribute, value, PropertyState.LOADED);
        }
    }

    private void writeObject(@Nonnull final ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        Type<E> type = RedisSerializationContext.getType(entityClass);
        EntityProxy<E> proxy = type.getProxyProvider().apply(entity);

        for (Attribute<E, ?> attribute : type.getAttributes()) {
            // currently only non-associative properties are serialized
            if (attribute.isAssociation()) {
                continue;
            }
            Object value = proxy.get(attribute, false);
            stream.writeObject(value);
        }
    }

    private static final long serialVersionUID = -6629533680424690266L;
}
