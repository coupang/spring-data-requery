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

package org.springframework.data.jpa.benchmark;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.benchmark.model.FullLog;
import org.springframework.data.jpa.benchmark.model.TagType;
import org.springframework.data.jpa.benchmark.model.TagValue;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JpaUtilsTest
 *
 * @author debop
 */
public class JpaUtilsTest {

    private static EntityManagerFactory emf = JpaUtils.getEntityManagerFactory();
    private static PlatformTransactionManager tm = new JpaTransactionManager(emf);
    private static TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    private static Random rnd = new Random(System.currentTimeMillis());

    private static FullLog randomFullLog() {
        FullLog fullLog = new FullLog();
        fullLog.setCreateAt(new Date());
        fullLog.setSystemId("SystemId:" + rnd.nextInt(1000));
        fullLog.setSystemName("SystemId:" + rnd.nextInt(1000));
        fullLog.setLogLevel(rnd.nextInt(5));
        fullLog.setThreadName("main-" + rnd.nextInt(16));

        return fullLog;
    }

    private EntityManager em;
    private TransactionStatus ts;

    @Before
    public void setup() {
        ts = tm.getTransaction(transactionDefinition);
        em = emf.createEntityManager();
        em.joinTransaction();
    }

    @After
    public void tearDown() {
        tm.commit(ts);
        if (em != null) {
            em.close();
        }
    }

    @Test
    public void createEntityManagerFactory() {

        assertThat(emf).isNotNull();

        EntityManager em = emf.createEntityManager();
        assertThat(em).isNotNull();
    }

    @Test
    public void insertFullLogWithEntityManager() {

        FullLog fullLog = randomFullLog();

        em.persist(fullLog);
        em.flush();

    }

    @Test
    public void insertTag() {
        TagValue tagValue = new TagValue();
        tagValue.setRep("rep");
        tagValue.setSynonyms("synonyms");
        tagValue.setCreatedAt(new Date());
        tagValue.setModifiedAt(new Date());

        TagType tagType = new TagType();
        tagType.setTagTypeClass("class");
        tagType.setTagTypeName("type");
        tagType.setCreatedAt(new Date());
        tagType.setModifiedAt(new Date());

        tagValue.setTagType(tagType);
        tagType.getTagValues().add(tagValue);

        em.persist(tagType);
        em.flush();

        assertThat(tagType.getTagTypeId()).isNotNull();
        assertThat(tagValue.getTagValueId()).isNotNull();
    }
}
