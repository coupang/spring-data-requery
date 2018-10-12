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

import io.requery.Embedded;
import io.requery.OneToOne;
import io.requery.Transient;
import io.requery.meta.EntityModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.annotation.Version;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

import javax.persistence.Access;
import javax.persistence.AccessType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * org.springframework.data.requery.mapping.RequeryPersistentPropertyTest
 *
 * @author debop
 * @since 18. 6. 8
 */
@RunWith(MockitoJUnitRunner.class)
public class RequeryPersistentPropertyTest {

    @Mock EntityModel entityModel;

    RequeryMappingContext context;
    RequeryPersistentEntity<?> entity;

    @Before
    public void setup() {
        context = new RequeryMappingContext();
        entity = context.getRequiredPersistentEntity(Sample.class);
    }

    @Test
    public void considersOneToOneMappedPropertyAssociation() {
        RequeryPersistentProperty property = entity.getRequiredPersistentProperty("other");
        assertThat(property).isNotNull();
        assertThat(property.isAssociation()).isTrue();
    }

    @Test
    public void considersTransientFieldsAsTransient() {
        RequeryPersistentProperty property = entity.getPersistentProperty("transientProp");
        assertThat(property).isNull();
    }

    @Test
    public void considersEmbeddedablePropertyAnAssociation() {
        assertThat(context.getPersistentEntity(SampleEmbeddable.class)).isNotNull();
    }

    @Test
    public void doesNotConsiderAnEmbeddablePropertyAnAssociation() {
        assertThat(entity.getPersistentProperty("embeddable").isAssociation()).isFalse();
    }

    @Test
    public void doesNotConsiderAnEmbeddedPropertyAnAssociation() {
        assertThat(entity.getPersistentProperty("embedded").isAssociation()).isFalse();
    }

    @Test
    public void considersPropertyLevelAccessTypeDefinitions() {
        assertThat(getProperty(PropertyLevelPropertyAccess.class, "field").usePropertyAccess()).isFalse();
        assertThat(getProperty(PropertyLevelPropertyAccess.class, "property").usePropertyAccess()).isTrue();
    }

    @Test
    public void propertyLevelAccessTypeTrumpsTypeLevelDefinition() {
        assertThat(getProperty(PropertyLevelDefinitionTrumpsTypeLevelOne.class, "field").usePropertyAccess()).isFalse();
        assertThat(getProperty(PropertyLevelDefinitionTrumpsTypeLevelOne.class, "property").usePropertyAccess()).isTrue();

        // JPA 는 상관하지 않습니다^^
        assertThat(getProperty(PropertyLevelDefinitionTrumpsTypeLevelOne2.class, "field").usePropertyAccess()).isTrue();
        assertThat(getProperty(PropertyLevelDefinitionTrumpsTypeLevelOne2.class, "property").usePropertyAccess()).isTrue();
    }

    @Test
    public void considersJpaAccessDefinitionAnnotations() {
        assertThat(getProperty(TypeLevelPropertyAccess.class, "id").usePropertyAccess()).isTrue();
    }

    @Test // DATAJPA-619
    public void springDataAnnotationTrumpsJpaIfBothOnTypeLevel() {
        assertThat(getProperty(CompetingTypeLevelAnnotations.class, "id").usePropertyAccess()).isFalse();
    }

    @Test // DATAJPA-619
    public void springDataAnnotationTrumpsJpaIfBothOnPropertyLevel() {
        assertThat(getProperty(CompetingPropertyLevelAnnotations.class, "id").usePropertyAccess()).isFalse();
    }

    @Test
    public void detectRequeryVersionAnnotation() {
        assertThat(getProperty(RequeryVersioned.class, "id").isIdProperty()).isTrue();

        assertThat(getProperty(SpringDataVersioned.class, "version").isVersionProperty()).isFalse();
        assertThat(getProperty(RequeryVersioned.class, "version").isVersionProperty()).isTrue();
    }

    @Test
    public void considersTargetEntityTypeForPropertyType() {
        RequeryPersistentProperty property = getProperty(SpecializedAssociation.class, "api");

        assertThat(property.getType()).isAssignableFrom(Api.class);

        Iterable<? extends TypeInformation<?>> entityTypes = property.getPersistentEntityTypes();
        assertThat(entityTypes.iterator().hasNext()).isTrue();
        assertThat(entityTypes.iterator().next()).isEqualTo(ClassTypeInformation.from(Api.class));
    }


    private RequeryPersistentProperty getProperty(Class<?> ownerType, String propertyName) {
        RequeryPersistentEntity<?> entity = context.getRequiredPersistentEntity(ownerType);
        return entity.getRequiredPersistentProperty(propertyName);
    }

    static class Sample {

        @OneToOne Sample other;
        @Transient String transientProp;

        SampleEmbeddable embeddable;

        @Embedded
        SampleEmbedded embedded;
    }

    @Embedded
    static class SampleEmbeddable {
    }

    static class SampleEmbedded {
    }

    @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.PROPERTY)
    static class TypeLevelPropertyAccess {
        private String id;

        public String getId() { return id; }
    }

    static class PropertyLevelPropertyAccess {

        String field;
        String property;

        @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.PROPERTY)
        public String getProperty() {
            return property;
        }
    }

    @Access(AccessType.FIELD)
    static class PropertyLevelDefinitionTrumpsTypeLevelOne {
        String field;
        String property;

        @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.PROPERTY)
        public String getProperty() {
            return property;
        }
    }

    @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.PROPERTY)
    static class PropertyLevelDefinitionTrumpsTypeLevelOne2 {
        @Access(AccessType.FIELD)
        String field;

        String property;

        public String getProperty() {
            return property;
        }
    }

    @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.FIELD)
    @Access(AccessType.PROPERTY)
    static class CompetingTypeLevelAnnotations {

        private String id;

        public String getId() { return id; }
    }

    @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.FIELD)
    @Access(AccessType.PROPERTY)
    static class CompetingPropertyLevelAnnotations {

        private String id;

        @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.FIELD)
        @Access(AccessType.PROPERTY)
        public String getId() {
            return id;
        }
    }

    static class SpringDataVersioned {

        @Version long version;
    }

    static class RequeryVersioned {

        @io.requery.Key Long id;

        @io.requery.Version long version;
    }

    static class SpecializedAssociation {

        @io.requery.ManyToOne Api api;
    }

    static interface Api {}

    static class Implementation {}

    static class WithIndexed {
        @io.requery.Column(index = true) String name;
        String updatable;
    }
}
