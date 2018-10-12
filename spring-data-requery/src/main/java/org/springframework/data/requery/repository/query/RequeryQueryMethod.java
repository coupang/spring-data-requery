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

package org.springframework.data.requery.repository.query;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.requery.annotation.Query;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Requery specific extension of {@link QueryMethod}.
 *
 * @author debop
 * @since 18. 6. 7
 */
@Getter
@Slf4j
public class RequeryQueryMethod extends QueryMethod {

    private static final Set<Class<?>> NATIVE_ARRAY_TYPES;

    static {
        Set<Class<?>> types = new HashSet<>();
        types.add(byte[].class);
        types.add(Byte[].class);
        types.add(char[].class);
        types.add(Character[].class);

        NATIVE_ARRAY_TYPES = Collections.unmodifiableSet(types);
    }

    private final Method method;
    private final RepositoryMetadata metadata;
    private final RequeryEntityMetadata<?> entityInformation;

    /**
     * Creates a new {@link QueryMethod} from the given parameters. Looks up the correct query to use for following
     * invocations of the queryMethod given.
     *
     * @param method   must not be {@literal null}.
     * @param metadata must not be {@literal null}.
     * @param factory  must not be {@literal null}.
     */
    public RequeryQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);

        Assert.notNull(method, "Method must not be null!");

        log.debug("Create RequeryQueryMethod. repository={}, methodName={}, queryMethod={}",
                  metadata.getRepositoryInterface(), method.getName(), method);

        this.method = method;
        this.metadata = metadata;
        this.entityInformation = DefaultRequeryEntityMetadata.of(getDomainClass());

        Assert.isTrue(!(isModifyingQuery() && getParameters().hasSpecialParameter()),
                      String.format("Modifying queryMethod must not contains %s!", Parameters.TYPES));

        assertParamterNamesInAnnotatedQuery();
    }

    private void assertParamterNamesInAnnotatedQuery() {

        String annotatedQuery = getAnnotatedQuery();

        for (Parameter parameter : getParameters()) {
            if (!parameter.isNamedParameter()) {
                continue;
            }

            String paramName = parameter.getName().orElse("");
            if (StringUtils.isEmpty(annotatedQuery)
                || StringUtils.hasText(paramName)
                   && !annotatedQuery.contains(":" + paramName)
                   && !annotatedQuery.contains("#" + paramName)) {
                throw new IllegalStateException(
                    String.format("Using named parameters for queryMethod [%s] but parameter '%s' not found in annotated query '%s'!",
                                  method, parameter.getName(), annotatedQuery));
            }
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public RequeryEntityMetadata<?> getEntityInformation() {
        return this.entityInformation;
    }

    @Override
    public boolean isModifyingQuery() {
        return false;
    }

    public boolean isAnnotatedQuery() {
        return null != AnnotationUtils.findAnnotation(method, Query.class);
    }

    public boolean isDefaultMethod() {
        return method.isDefault();
    }

    public boolean isOverridedMethod() {
        return !method.getDeclaringClass().equals(metadata.getRepositoryInterface());
    }

    Class<?> getReturnType() {
        return method.getReturnType();
    }

    @Nullable
    String getAnnotatedQuery() {
        String query = getAnnotationValue("value", String.class);
        return StringUtils.hasText(query) ? query : null;
    }

    String getRequiredAnnotatedQuery() throws IllegalStateException {
        String query = getAnnotatedQuery();
        if (query != null) {
            return query;
        }

        throw new IllegalStateException("No annotated query found for query queryMethod " + getName());
    }

    @Nullable
    String getCountQuery() {
        String countQuery = getAnnotationValue("countQuery", String.class);
        return StringUtils.hasText(countQuery) ? countQuery : null;
    }


    @SuppressWarnings("SameParameterValue")
    private String getAnnotationValue(String attribute, Class<String> type) {
        return getMergedOrDefaultAnnotationValue(attribute, Query.class, type);
    }

    @SuppressWarnings({ "unchecked", "SameParameterValue" })
    private <T> T getMergedOrDefaultAnnotationValue(String attribute,
                                                    Class annotationType,
                                                    Class<T> targetType) {
        Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (annotation == null) {
            return targetType.cast(AnnotationUtils.getDefaultValue(annotationType, attribute));
        }

        return targetType.cast(AnnotationUtils.getValue(annotation, attribute));
    }

    @NotNull
    @Override
    protected RequeryParameters createParameters(Method method) {
        return new RequeryParameters(method);
    }

    @Override
    public RequeryParameters getParameters() {
        return (RequeryParameters) super.getParameters();
    }

    @Override
    public boolean isCollectionQuery() {
        return super.isCollectionQuery() && !NATIVE_ARRAY_TYPES.contains(method.getReturnType());
    }

}
