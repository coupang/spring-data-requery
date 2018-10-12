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

package org.springframework.data.requery.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * DefaultRequeryPersistentProperty
 *
 * @author debop
 * @since 18. 6. 8
 */
@Slf4j
public class DefaultRequeryPersistentProperty
    extends AnnotationBasedPersistentProperty<RequeryPersistentProperty>
    implements RequeryPersistentProperty {

    private final FieldNamingStrategy fieldNamingStrategy;

    /**
     * Creates a new {@link AnnotationBasedPersistentProperty}.
     */
    public DefaultRequeryPersistentProperty(Property property,
                                            PersistentEntity<?, RequeryPersistentProperty> owner,
                                            SimpleTypeHolder simpleTypeHolder,
                                            FieldNamingStrategy fieldNamingStrategy) {
        super(property, owner, simpleTypeHolder);
        this.fieldNamingStrategy = (fieldNamingStrategy != null) ? fieldNamingStrategy : PropertyNameFieldNamingStrategy.INSTANCE;
    }

    @Override
    protected Association<RequeryPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }

    @Override
    public boolean isIdProperty() {
        return findAnnotation(io.requery.Key.class) != null;
    }

    @Override
    public boolean isEmbedded() {
        return findAnnotation(io.requery.Embedded.class) != null;
    }

    @Override
    public boolean hasIndex() {
        return findAnnotation(io.requery.Index.class) != null;
    }

    @Override
    public boolean isAssociation() {
        return findAnnotation(io.requery.OneToOne.class) != null ||
               findAnnotation(io.requery.OneToMany.class) != null ||
               findAnnotation(io.requery.ManyToOne.class) != null ||
               findAnnotation(io.requery.ManyToMany.class) != null;
    }

    @Override
    public boolean isTransient() {
        return findAnnotation(io.requery.Transient.class) != null;
    }

    @Override
    public boolean isVersionProperty() {
        return findAnnotation(io.requery.Version.class) != null;
    }

    @Override
    public String getFieldName() {
        final String fieldName;
        if (isIdProperty()) {
            fieldName = getProperty().getName();
        } else {
            fieldName = getAnnotatedColumnName().orElse(fieldNamingStrategy.getFieldName(this));
        }
        log.debug("property={}, fieldName={}", getProperty(), fieldName);
        return fieldName;
    }

    private Optional<String> getAnnotatedColumnName() {
        return Optional.ofNullable(findAnnotation(io.requery.Column.class))
            .map(name -> StringUtils.hasText(name.value()) ? name.value() : null);
    }


}
