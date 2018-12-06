package org.springframework.data.requery.core;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

/**
 * NamedPreparedQueryOperation
 * <p>
 * {@link io.requery.sql.PreparedQueryOperation}를 참고하여 raw 메소드에서도 Named parameter를 사용할 수 있도록 합니다.
 * <p>
 * ref: https://github.com/axiom-data-science/jdbc-named-parameters
 * ref: https://www.codemeright.com/blog/post/named-parameterized-query-java
 * <p>
 * 차라리 Spring 에서 제공하는 NamedParameterJdbcTemplate 를 사용하는 것이 나을 수 있다.
 * https://www.devglan.com/spring-jdbc/working-with-springboot-namedparameter-jdbctemplate
 *
 * @author debop
 * @since 18. 12. 5
 */
public abstract class NamedPreparedQueryOperation {

    private NamedParameterJdbcOperations jdbcOperations;
}
