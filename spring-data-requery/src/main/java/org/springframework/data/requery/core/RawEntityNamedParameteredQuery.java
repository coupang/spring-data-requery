package org.springframework.data.requery.core;

/**
 * {@link io.requery.sql.RawTupleQuery} 은 Named parameter를 지원하지 않아, {@link NamedPreparedQueryOperation} 을 상속해서
 * named parameter를 이용한 query를 수행할 수 있도록 합니다.
 *
 * @author debop
 * @since 18. 12. 5
 */
public class RawEntityNamedParameteredQuery extends NamedPreparedQueryOperation {
}
