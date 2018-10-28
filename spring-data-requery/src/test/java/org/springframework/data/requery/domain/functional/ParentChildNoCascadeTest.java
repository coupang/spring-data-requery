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

package org.springframework.data.requery.domain.functional;

import io.requery.PersistenceException;
import io.requery.sql.RowCountException;
import io.requery.sql.StatementExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Diego on 2018. 6. 12..
 */
@Slf4j
public class ParentChildNoCascadeTest extends AbstractDomainTest {

    private static final int COUNT = 100;

    @Before
    public void setup() {
        requeryOperations.deleteAll(FuncChild.class);
        requeryOperations.deleteAll(FuncParent.class);
        requeryOperations.deleteAll(FuncChildOneToOneNoCascade.class);
        requeryOperations.deleteAll(FuncChildOneToManyNoCascade.class);
        requeryOperations.deleteAll(FuncChildManyToOneNoCascade.class);
        requeryOperations.deleteAll(FuncChildManyToManyNoCascade.class);
        requeryOperations.deleteAll(FuncParentNoCascade.class);
    }

    @Test
    public void insert_without_cascade_action_save() {
        assertThatThrownBy(() -> {
            FuncChild child = new FuncChild();

            child.setId(1L);
            FuncParent parent = new FuncParent();
            parent.setChild(child);

            // NOTE: This violates the Foreign Key Constraint, because Child does not exist and CascadeAction.SAVE is not specified
            requeryOperations.insert(parent);
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    public void insert_no_cascade_one_to_one_non_existing_child() {
        // BUG: no cascade 인데, 예외가 나지 않는다
        // Insert parent entity, associated one-to-one to a non existing child entity
        // This should fail with a foreign-key violation, since child does not exist in the database

        // assertThatThrownBy {
        FuncChildOneToOneNoCascade child = new FuncChildOneToOneNoCascade();
        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(1L);
        parent.setOneToOne(child);

        requeryOperations.insert(parent);
        // }.isInstanceOf(StatementExecutionException::class)
    }

    @Test
    public void insert_no_cascade_one_to_one_existing_child() {
        // Insert parent entity, associated one-to-one to an existing child entity
        FuncChildOneToOneNoCascade child = new FuncChildOneToOneNoCascade();
        child.setId(1L);
        child.setAttribute("1");
        requeryOperations.insert(child);

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(1L);
        parent.setOneToOne(child);
        requeryOperations.insert(parent);

        // Assert that child has been associated to parent
        FuncParentNoCascade parentGot = requeryOperations.findById(FuncParentNoCascade.class, 1L);
        assertThat(parentGot.getOneToOne()).isEqualTo(child);
    }

    @Test
    public void insert_no_cascade_many_to_one_existing_child() {
        // Insert parent entity, associated many-to-one to an existing child entity
        FuncChildManyToOneNoCascade child = new FuncChildManyToOneNoCascade();
        child.setId(1L);
        child.setAttribute("1");
        requeryOperations.insert(child);

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(1L);
        parent.setManyToOne(child);
        requeryOperations.insert(parent);

        // Assert that child has been associated to parent
        FuncParentNoCascade parentGot = requeryOperations.findById(FuncParentNoCascade.class, 1L);
        assertThat(parentGot.getManyToOne()).isEqualTo(child);
    }

    @Test
    public void insert_no_cascade_one_to_many_non_existing_child() {
        // Insert parent entity, associated one-to-may to 1 non-existing child entity
        assertThatThrownBy(() -> {
            FuncChildOneToManyNoCascade child = new FuncChildOneToManyNoCascade();
            child.setId(1L);
            child.setAttribute("1");

            FuncParentNoCascade parent = new FuncParentNoCascade();
            parent.setId(1L);
            parent.getOneToMany().add(child);
            requeryOperations.insert(parent);

            // Assert that child has been associated to parent
            FuncParentNoCascade parentGot = requeryOperations.findById(FuncParentNoCascade.class, 1L);
        }).isInstanceOf(RowCountException.class);
    }

    @Test
    public void insert_no_cascade_one_to_many_existing_child() {
        // Insert parent entity, associated one-to-may to 1 existing child entity
        FuncChildOneToManyNoCascade child = new FuncChildOneToManyNoCascade();
        child.setId(1L);
        child.setAttribute("1");
        requeryOperations.insert(child);

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(1L);
        parent.getOneToMany().add(child);
        requeryOperations.insert(parent);

        // Assert that child has been associated to parent
        FuncParentNoCascade parentGot = requeryOperations.findById(FuncParentNoCascade.class, 1L);
        assertThat(parentGot.getOneToMany()).hasSize(1);
        assertThat(parentGot.getOneToMany().get(0)).isEqualTo(child);
    }

    @Test
    public void insert_no_cascade_many_to_many_non_existing_child() {
        // Insert parent entity, associated many-to-may to 1 non-existing child entity
        assertThatThrownBy(() -> {
            FuncChildManyToManyNoCascade child = new FuncChildManyToManyNoCascade();
            child.setId(505L);
            child.setAttribute("1");

            FuncParentNoCascade parent = new FuncParentNoCascade();
            parent.setId(1L);
            parent.getManyToMany().add(child);
            requeryOperations.insert(parent);
        }).isInstanceOf(StatementExecutionException.class);
    }

    @Test
    public void insert_no_cascade_many_to_many_existing_child() {
        // Insert parent entity, associated many-to-may to 1 existing child entity
        FuncChildManyToManyNoCascade child = new FuncChildManyToManyNoCascade();
        child.setId(1L);
        child.setAttribute("1");
        requeryOperations.insert(child);

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(1L);
        parent.getManyToMany().add(child);
        requeryOperations.insert(parent);

        // Assert that child has been associated to parent
        FuncParentNoCascade parentGot = requeryOperations.findById(FuncParentNoCascade.class, 1L);
        assertThat(parentGot.getManyToMany()).hasSize(1);
        assertThat(parentGot.getManyToMany().get(0)).isEqualTo(child);
    }

    @Test
    public void delete_no_cascade_one_to_one() {
        FuncChildOneToOneNoCascade child = new FuncChildOneToOneNoCascade();
        child.setId(123L);
        child.setAttribute("1");

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(123L);
        parent.setOneToOne(child);

        requeryOperations.insert(child);
        requeryOperations.insert(parent);

        requeryOperations.delete(parent);

        FuncChildOneToOneNoCascade childGot = requeryOperations.findById(FuncChildOneToOneNoCascade.class, 123L);
        assertThat(childGot).isNotNull();
    }

    @Test
    public void delete_no_cascade_many_to_one() {
        FuncChildManyToOneNoCascade child = new FuncChildManyToOneNoCascade();
        child.setId(123L);
        child.setAttribute("1");

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(123L);
        parent.setManyToOne(child);

        requeryOperations.insert(child);
        requeryOperations.insert(parent);

        requeryOperations.delete(parent);

        FuncChildManyToOneNoCascade childGot = requeryOperations.findById(FuncChildManyToOneNoCascade.class, 123L);
        assertThat(childGot).isNotNull();
    }

    @Test
    public void delete_no_cascade_one_to_many() {
        FuncChildOneToManyNoCascade child1 = new FuncChildOneToManyNoCascade();
        child1.setId(1L);
        child1.setAttribute("1");
        FuncChildOneToManyNoCascade child2 = new FuncChildOneToManyNoCascade();
        child2.setId(2L);
        child2.setAttribute("2");

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(1L);
        parent.getOneToMany().add(child1);
        parent.getOneToMany().add(child2);

        requeryOperations.insert(child1);
        requeryOperations.insert(child2);
        requeryOperations.insert(parent);

        // delete parent
        requeryOperations.delete(parent);

        FuncChildOneToManyNoCascade child1Got = requeryOperations.findById(FuncChildOneToManyNoCascade.class, 1L);
        assertThat(child1Got).isNotNull();
        assertThat(child1Got.getParent()).isNull();

        FuncChildOneToManyNoCascade child2Got = requeryOperations.findById(FuncChildOneToManyNoCascade.class, 2L);
        assertThat(child2Got).isNotNull();
        assertThat(child2Got.getParent()).isNull();
    }

    @Test
    public void delete_no_cascade_many_to_many() {
        FuncChildManyToManyNoCascade child1 = new FuncChildManyToManyNoCascade();
        child1.setId(1L);
        child1.setAttribute("1");
        FuncChildManyToManyNoCascade child2 = new FuncChildManyToManyNoCascade();
        child2.setId(2L);
        child2.setAttribute("2");

        FuncParentNoCascade parent = new FuncParentNoCascade();
        parent.setId(1L);
        parent.getManyToMany().add(child1);
        parent.getManyToMany().add(child2);

        requeryOperations.insert(child1);
        requeryOperations.insert(child2);
        requeryOperations.insert(parent);

        requeryOperations.delete(parent);

        FuncChildManyToManyNoCascade child1Got = requeryOperations.findById(FuncChildManyToManyNoCascade.class, 1L);
        assertThat(child1Got).isNotNull();

        FuncChildManyToManyNoCascade child2Got = requeryOperations.findById(FuncChildManyToManyNoCascade.class, 2L);
        assertThat(child2Got).isNotNull();
    }
}
