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

package org.springframework.data.requery.utils;

import io.requery.Embedded;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import io.requery.OneToOne;
import io.requery.Transient;
import io.requery.meta.Attribute;
import io.requery.meta.EntityModel;
import io.requery.meta.Type;
import io.requery.query.Condition;
import io.requery.query.Expression;
import io.requery.query.LogicalCondition;
import io.requery.query.NamedExpression;
import io.requery.query.OrderingExpression;
import io.requery.query.Return;
import io.requery.query.WhereAndOr;
import io.requery.query.element.LogicalOperator;
import io.requery.query.element.QueryElement;
import io.requery.query.element.QueryWrapper;
import io.requery.query.element.WhereConditionElement;
import io.requery.sql.EntityContext;
import io.requery.sql.EntityDataStore;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Requery 사용을 위한 Utility class
 *
 * @author debop
 * @since 18. 6. 20
 */
@Slf4j
@UtilityClass
public class RequeryUtils {

    private static final Map<Class<?>, NamedExpression<?>> classKeys = new ConcurrentHashMap<>();
    public static final NamedExpression<?> UNKNOWN_KEY_EXPRESSION = NamedExpression.of("Unknown", Object.class);

    @NotNull
    public static NamedExpression<?> getKeyExpression(@NotNull final Class<?> domainClass) {
        Assert.notNull(domainClass, "domainClass must not be null!");
        log.trace("Retrieve Key property. domainClass={}", domainClass.getSimpleName());

        return classKeys
            .computeIfAbsent(domainClass, (clazz) -> {
                Field field = RequeryUtils.findFirstField(clazz, it -> it.getAnnotation(Key.class) != null);
                return (field != null)
                       ? NamedExpression.of(field.getName(), field.getType())
                       : UNKNOWN_KEY_EXPRESSION;
            });
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    public static EntityContext getEntityContext(@NotNull final EntityDataStore entityDataStore) {
        Assert.notNull(entityDataStore, "entityDataStore must not be null!");

        try {
            Field field = ReflectionUtils.findField(entityDataStore.getClass(), "context");
            Assert.notNull(field, "context field must not be null!");
            field.setAccessible(true);

            return (EntityContext) ReflectionUtils.getField(field, entityDataStore);
        } catch (Exception e) {
            throw new IllegalStateException("Fail to retrieve EntityContext.");
        }
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    public static EntityModel getEntityModel(@NotNull final EntityDataStore entityDataStore) {
        Assert.notNull(entityDataStore, "entityDataStore must not be null!");

        try {
            Field field = ReflectionUtils.findField(entityDataStore.getClass(), "entityModel");
            Assert.notNull(field, "entityModel field must not be null!");
            field.setAccessible(true);

            return (EntityModel) ReflectionUtils.getField(field, entityDataStore);
        } catch (Exception e) {
            throw new IllegalStateException("Fail to retrieve entity model.", e);
        }
    }

    @NotNull
    public static Set<Type<?>> getEntityTypes(@NotNull final EntityDataStore entityDataStore) {
        Assert.notNull(entityDataStore, "entityDataStore must not be null!");

        EntityModel model = getEntityModel(entityDataStore);
        Assert.notNull(model, "Not found EntityModel!");

        return model.getTypes();
    }

    @NotNull
    public static List<Class<?>> getEntityClasses(@NotNull final EntityDataStore entityDataStore) {
        Assert.notNull(entityDataStore, "entityDataStore must not be null!");

        return getEntityTypes(entityDataStore)
            .stream()
            .map(Type::getClassType)
            .collect(toList());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E> Type<E> getType(@NotNull final EntityDataStore entityDataStore,
                                      @NotNull final Class<E> entityClass) {
        Assert.notNull(entityDataStore, "entityDataStore must not be null!");
        Assert.notNull(entityClass, "entityClass must not be null!");

        Set<Type<?>> types = getEntityTypes(entityDataStore);

        return (Type<E>) types
            .stream()
            .filter(it -> entityClass.equals(it.getClassType()))
            .findFirst()
            .orElse(null);
    }

    public static <E> Set<? extends Attribute<E, ?>> getKeyAttributes(@NotNull final EntityDataStore entityDataStore,
                                                                      @NotNull final Class<E> entityClass) {
        Assert.notNull(entityDataStore, "entityDataStore must not be null!");
        Assert.notNull(entityClass, "entityClass must not be null!");

        Type<E> type = getType(entityDataStore, entityClass);
        return (type != null) ? type.getKeyAttributes() : Collections.emptySet();
    }

    /**
     * 지정한 엔티티 수형의 {@link Key}가 지정된 속성 정보를 조회한다
     *
     * @param entityDataStore requery entityDataStore
     * @param entityClass     entity class type to get key field
     * @param <E>             entity type
     * @return {@link Attribute} of Key property
     */
    @Nullable
    public static <E> Attribute<E, ?> getSingleKeyAttribute(@NotNull final EntityDataStore entityDataStore,
                                                            @NotNull final Class<E> entityClass) {
        Assert.notNull(entityDataStore, "entityDataStore must not be null!");
        Assert.notNull(entityClass, "entityClass must not be null!");

        Type<E> type = getType(entityDataStore, entityClass);
        return (type != null) ? type.getSingleKeyAttribute() : null;
    }

    /**
     * 모든 Requery Query 구문은 {@link QueryWrapper} 를 구현하고 내부에 {@link QueryElement} 를 가지고 있습니다.
     * 이를 unwrap 해서 실제 {@link QueryElement} 를 반환하도록 합니다.
     *
     * @param wrapper {@link QueryWrapper} instance
     * @return {@link QueryElement} instance
     */
    @SuppressWarnings("ConstantConditions")
    @NotNull
    public static QueryElement<?> unwrap(@NotNull final Return<?> wrapper) {
        if (wrapper instanceof QueryWrapper) {
            return ((QueryWrapper<?>) wrapper).unwrapQuery();
        } else {
            return (QueryElement<?>) wrapper;
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <E> QueryElement<?> applyPageable(@NotNull final Class<E> domainClass,
                                                    @NotNull final QueryElement<?> baseQuery,
                                                    @NotNull final Pageable pageable) {
        Assert.notNull(domainClass, "domainClass must not be null!");
        Assert.notNull(baseQuery, "baseQuery must not be null!");
        Assert.notNull(pageable, "pageable must not be null!");

        if (pageable.isUnpaged()) {
            return baseQuery;
        }

        log.trace("Apply paging, domainClass={}, pageable={}", domainClass.getSimpleName(), pageable);

        QueryElement<?> query = baseQuery;

        if (pageable.getSort().isSorted()) {
            query = applySort(domainClass, query, pageable.getSort());
        }
        if (pageable.getPageSize() > 0 && query.getLimit() == null) {
            query = unwrap(query.limit(pageable.getPageSize()));
        }
        if (pageable.getOffset() > 0 && query.getOffset() == null) {
            query = unwrap(query.offset((int) pageable.getOffset()));
        }

        return query;
    }

    /**
     * baseQuery에 sort 조건을 추가합니다.
     *
     * @param domainClass type of domain entity class
     * @param baseQuery   base query to add sort
     * @param sort        sort
     * @param <E>         entity type
     * @return {@link QueryElement} which added {@link Sort}
     */
    @SuppressWarnings("unchecked")
    public static <E> QueryElement<?> applySort(@NotNull final Class<E> domainClass,
                                                @NotNull final QueryElement<?> baseQuery,
                                                @NotNull final Sort sort) {
        log.trace("Apply sort, domainClass={}, sort={}", domainClass.getSimpleName(), sort);

        QueryElement<?> query = baseQuery;

        if (sort.isUnsorted()) {
            return query;
        }

        for (Sort.Order order : sort) {

            final String propertyName = order.getProperty();
            final Sort.Direction direction = order.getDirection();

            // 이미 있을 수 있다 ...
            Expression<?> orderExpr = null;
            if (query.getOrderByExpressions() != null) {
                orderExpr = query.getOrderByExpressions()
                    .stream()
                    .filter(it -> propertyName.equals(it.getName()))
                    .findFirst()
                    .orElse(null);
            }

            if (orderExpr == null) {
                Field field = findField(domainClass, propertyName);
                if (field != null) {
                    NamedExpression<?> expr = NamedExpression.of(propertyName, field.getType());
                    query = unwrap(query.orderBy(direction.isAscending() ? expr.asc() : expr.desc()));
                }
            }
        }

        return query;
    }

    public static @NotNull OrderingExpression<?>[] getOrderingExpressions(@NotNull final Class<?> domainClass,
                                                                          @Nullable final Sort sort) {
        Assert.notNull(domainClass, "domainClass must not null.");

        if (sort == null || sort.isUnsorted()) {
            return new OrderingExpression[0];
        }

        List<OrderingExpression<?>> orderingExprs = new ArrayList<>();

        for (Sort.Order order : sort) {
            String propertyName = order.getProperty();
            Sort.Direction direction = order.getDirection();

            Field field = findField(domainClass, propertyName);
            if (field != null) {
                NamedExpression<?> expr = NamedExpression.of(propertyName, field.getType());
                OrderingExpression<?> orderingExpr = (order.isAscending()) ? expr.asc() : expr.desc();
                orderingExprs.add(orderingExpr);
            }
        }

        return orderingExprs.toArray(new OrderingExpression<?>[0]);
    }

    @SuppressWarnings("unchecked")
    public static <E> @Nullable LogicalCondition<E, ?> foldConditions(@NotNull final Iterable<Condition<E, ?>> conditions) {
        LogicalCondition<E, ?> condition = null;
        for (Condition<E, ?> cond : conditions) {
            if (condition == null) {
                condition = (LogicalCondition<E, ?>) cond;
            } else {
                condition.and(cond);
            }
        }
        return condition;
    }

    @SuppressWarnings("unchecked")
    public static <E> @Nullable LogicalCondition<E, ?> foldConditions(@NotNull final Iterable<Condition<E, ?>> conditions,
                                                                      @NotNull final LogicalOperator operator) {
        LogicalCondition<E, ?> condition = null;
        for (Condition<E, ?> cond : conditions) {
            if (condition == null) {
                condition = (LogicalCondition<E, ?>) cond;
            } else {
                switch (operator) {
                    case AND:
                        condition.and(cond);
                        break;
                    case OR:
                        condition.or(cond);
                        break;
                    case NOT:
                        condition.and(((LogicalCondition<E, ?>) cond).not());
                }
            }
        }
        return condition;
    }

    @NotNull
    public static QueryElement<?> applyWhereClause(@NotNull final QueryElement<?> baseQuery,
                                                   @NotNull final Set<WhereConditionElement<?>> conditionElements) {
        if (conditionElements.isEmpty()) {
            return baseQuery;
        } else if (conditionElements.size() == 1) {
            return unwrap(baseQuery.where(conditionElements.iterator().next().getCondition()));
        } else {
            WhereAndOr<?>[] whereClause = new WhereAndOr[1];
            WhereConditionElement<?> firstElement = conditionElements.stream().findFirst().orElseThrow(NoSuchElementException::new);
            whereClause[0] = baseQuery.where(firstElement.getCondition());
            // LogicalOperator prevOperator = firstElement.getOperator();

            conditionElements.stream()
                .skip(1)
                .forEach(conditionElement -> {
                    Condition<?, ?> condition = conditionElement.getCondition();
                    LogicalOperator operator = conditionElement.getOperator();

                    log.trace("Where condition={}, operator={}", condition, operator);

                    switch (operator) {
                        case AND:
                            whereClause[0] = whereClause[0].and(condition);
                            break;
                        case OR:
                            whereClause[0] = whereClause[0].or(condition);
                            break;
                        case NOT:
                            whereClause[0] = whereClause[0].and(condition).not();
                            break;
                        default:
                            // Nothing to do.
                    }
                });

            return unwrap(whereClause[0]);
        }
    }

    /**
     * Cache for Field of Class
     */
    private static Map<String, Field> classFieldCache = new ConcurrentHashMap<>();

    /**
     * 지정된 클래스의 특정 필드명을 가진 {@link Field} 정보를 가져온다. 없다면 null 반환
     *
     * @param domainClass 대상 클래스 수형
     * @param fieldName   필드명
     * @return {@link Field} 정보를 가져온다. 없다면 null 반환
     */
    @Nullable
    public static Field findField(@NotNull final Class<?> domainClass,
                                  @NotNull final String fieldName) {

        Assert.notNull(domainClass, "domainClass must not be null!");
        Assert.hasText(fieldName, "fieldName must not be empty!");

        final String cacheKey = domainClass.getName() + "." + fieldName;

        return classFieldCache.computeIfAbsent(cacheKey, key -> {
            Class<?> targetClass = domainClass;

            do {
                try {
                    Field foundField = targetClass.getDeclaredField(fieldName);
                    if (foundField != null) {
                        return foundField;
                    }
                } catch (Exception e) {
                    log.debug("Fail to retrieve field. search in superClass... fieldName={} in targetClass={}", fieldName, targetClass);
                }
                targetClass = targetClass.getSuperclass();
            } while (targetClass != null && targetClass != Object.class);

            return null;
        });
    }

    /**
     * 지정된 클래스에서 원하는 Field 정보만을 가져온다.
     *
     * @param domainClass    대상 Class 정보
     * @param fieldPredicate 원하는 {@link Field} 인지 판단하는 predicate
     * @return 조건에 맞는 Field 정보
     */
    @NotNull
    public static List<Field> findFields(@NotNull final Class<?> domainClass,
                                         @NotNull final Predicate<Field> fieldPredicate) {
        Assert.notNull(domainClass, "domainClass must not be null!");
        Assert.notNull(fieldPredicate, "predicate must not be null!");

        List<Field> foundFields = new ArrayList<>();
        Class<?> targetClass = domainClass;

        do {
            Field[] fields = targetClass.getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (fieldPredicate.test(field)) {
                        foundFields.add(field);
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return foundFields;
    }

    @Nullable
    public static Field findFirstField(@NotNull final Class<?> domainClass,
                                       @NotNull final Predicate<Field> fieldPredicate) {
        Assert.notNull(domainClass, "domainClass must not be null!");
        Assert.notNull(fieldPredicate, "predicate must not be null!");

        Class<?> targetClass = domainClass;

        do {
            Field[] fields = targetClass.getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (fieldPredicate.test(field)) {
                        return field;
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return null;
    }

    /**
     *
     */
    private static MultiValueMap<Class<?>, Field> entityFields = new LinkedMultiValueMap<>();

    /**
     * Requery Entity 에서 독립적인 컬럼 역할을 수행하는 Field 만 가져옵니다.
     *
     * @param domainClass Requery 엔티티의 클래스
     * @return Field collection
     */
    @NotNull
    public static List<Field> findEntityFields(@NotNull final Class<?> domainClass) {
        return entityFields.computeIfAbsent(domainClass, clazz ->
            findFields(clazz, RequeryUtils::isRequeryEntityField)
        );
    }

    public static boolean isRequeryEntityField(@NotNull final Field field) {
        return !isRequeryGeneratedField(field);
    }

    public static boolean isRequeryGeneratedField(@NotNull final Field field) {
        String fieldName = field.getName();

        return (field.getModifiers() & Modifier.STATIC) > 0 ||
               "$proxy".equals(fieldName) ||
               (fieldName.startsWith("$") && fieldName.endsWith("_state"));
    }

    public static boolean isTransientField(@NotNull final Field field) {
        return field.isAnnotationPresent(Transient.class);
    }

    public static boolean isEmbededField(@NotNull final Field field) {
        return field.isAnnotationPresent(Embedded.class);
    }

    public static boolean isAssociationField(@NotNull final Field field) {
        return field.isAnnotationPresent(OneToOne.class) ||
               field.isAnnotationPresent(OneToMany.class) ||
               field.isAnnotationPresent(ManyToOne.class) ||
               field.isAnnotationPresent(ManyToMany.class);
    }

}
