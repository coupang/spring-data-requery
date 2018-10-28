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
import io.requery.meta.NumericAttribute;
import io.requery.query.Expression;
import io.requery.query.NamedNumericExpression;
import io.requery.query.Result;
import io.requery.query.Return;
import io.requery.query.Tuple;
import io.requery.query.WhereAndOr;
import io.requery.query.function.Case;
import io.requery.query.function.Coalesce;
import io.requery.query.function.Count;
import io.requery.query.function.Random;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;
import org.springframework.data.requery.domain.model.Address;
import org.springframework.data.requery.domain.model.Group;
import org.springframework.data.requery.domain.model.Person;
import org.springframework.data.requery.domain.model.Phone;
import org.springframework.data.requery.domain.model.RandomData;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Diego on 2018. 6. 9..
 */
@Slf4j
public class FunctionalQueryTest extends AbstractDomainTest {

    private static final int COUNT = 100;

    @Before
    public void setup() {
        requeryOperations.deleteAll(Address.class);
        requeryOperations.deleteAll(Group.class);
        requeryOperations.deleteAll(Phone.class);
        requeryOperations.deleteAll(Person.class);
    }

    @Test
    public void query_function_now() {
        Person person = RandomData.randomPerson();
        person.setBirthday(LocalDate.now().plusDays(1));
        requeryOperations.insert(person);

        Result<Person> result = requeryOperations.select(Person.class)
            .where(Person.BIRTHDAY.gt(LocalDate.now()))
            .get();

        List<Person> people = result.toList();
        assertThat(people).hasSize(1);

        Integer count = requeryOperations.count(Person.class)
            .where(Person.BIRTHDAY.gt(LocalDate.now()))
            .get()
            .value();

        assertThat(people).hasSize(count);
    }

    @Test
    public void query_function_random() {
        Set<Person> people = RandomData.randomPeople(10);
        requeryOperations.insertAll(people);

        Result<Person> result = requeryOperations.select(Person.class)
            .orderBy(new Random())
            .get();

        assertThat(result.toList()).hasSize(10);
    }

    @Test
    public void single_query_where() {
        String name = "duplicateFirstMame";

        for (int i = 0; i < 10; i++) {
            Person person = RandomData.randomPerson();
            person.setName(name);
            requeryOperations.insert(person);
        }

        Result<Person> result = requeryOperations.select(Person.class)
            .where(Person.NAME.eq(name))
            .get();

        assertThat(result.toList()).hasSize(10);
    }

    @Test
    public void single_query_where_not() {
        String name = "firstName";
        String email = "not@test.io";

        for (int i = 0; i < 10; i++) {
            Person person = RandomData.randomPerson();
            switch (i) {
                case 0:
                    person.setName(name);
                    break;
                case 1:
                    person.setEmail(email);
                    break;
            }
            requeryOperations.insert(person);
        }

        // FIXME: not 연산자가 제대로 동작하지 않는다. (Java 와 Kotlin 모두)
        //
        Result<Person> result = requeryOperations.select(Person.class)
            .where(Person.NAME.ne(name).and(Person.EMAIL.ne(email)))
            .get();

        assertThat(result.toList()).hasSize(8);
    }

    @Test
    public void single_query_execute() {
        requeryOperations.insertAll(RandomData.randomPeople(10));

        Result<Person> result = requeryOperations.select(Person.class).get();
        assertThat(result.toList()).hasSize(10);

        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        assertThat(result.toList()).hasSize(11);
    }

    @Test
    public void single_query_with_limit_and_offset() {
        String name = "duplicateFirstName";

        for (int i = 0; i < 10; i++) {
            Person person = RandomData.randomPerson();
            person.setName(name);
            requeryOperations.insert(person);
        }

        for (int i = 0; i < 3; i++) {
            Result<Person> query = requeryOperations.select(Person.class)
                .where(Person.NAME.eq(name))
                .orderBy(Person.NAME)
                .limit(5)
                .get();
            assertThat(query.toList()).hasSize(5);

            Result<Person> query2 = requeryOperations.select(Person.class)
                .where(Person.NAME.eq(name))
                .limit(5)
                .offset(5)
                .get();
            assertThat(query2.toList()).hasSize(5);
        }
    }

    @Test
    public void single_query_where_null() {
        Person person = RandomData.randomPerson();
        person.setName(null);
        requeryOperations.insert(person);

        Result<Person> query = requeryOperations.select(Person.class)
            .where(Person.NAME.isNull())
            .get();

        assertThat(query.toList()).hasSize(1);
    }

    @Test
    public void delete_all() {
        String name = "someName";

        for (int i = 0; i < 10; i++) {
            Person person = RandomData.randomPerson();
            person.setName(name);
            requeryOperations.insert(person);
        }

        assertThat(requeryOperations.deleteAll(Person.class)).isGreaterThan(0);
        assertThat(requeryOperations.select(Person.class).get().firstOrNull()).isNull();
    }

    @Test
    public void delete_batch() {
        Set<Person> people = RandomData.randomPeople(COUNT);
        requeryOperations.insertAll(people);

        assertThat(requeryOperations.count(Person.class).get().value()).isEqualTo(people.size());

        requeryOperations.deleteAll(people);
        assertThat(requeryOperations.count(Person.class).get().value()).isEqualTo(0);
    }

    @Test
    public void query_by_foreign_key() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone1);
        person.getPhoneNumbers().add(phone2);

        requeryOperations.upsert(person);
        assertThat(person.getPhoneNumberSet()).containsOnly(phone1, phone2);

        // by entity
        Result<Phone> query1 = requeryOperations.select(Phone.class).where(Phone.OWNER.eq(person)).get();

        assertThat(query1.toList()).hasSize(2).containsOnly(phone1, phone2);
        assertThat(person.getPhoneNumberList()).hasSize(2).containsAll(query1.toList());

        // by id
        Result<Phone> query2 = requeryOperations.select(Phone.class).where(Phone.OWNER_ID.eq(person.getId())).get();

        assertThat(query2.toList()).hasSize(2).containsOnly(phone1, phone2);
        assertThat(person.getPhoneNumberList()).hasSize(2).containsAll(query2.toList());
    }

    @Test
    public void query_by_UUID() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        UUID uuid = person.getUUID();
        Person loaded = requeryOperations.select(Person.class).where(Person.UUID.eq(uuid)).get().first();
        assertThat(loaded).isEqualTo(person);
    }

    @Test
    public void query_select_distinct() {
        for (int i = 0; i < 10; i++) {
            Person person = RandomData.randomPerson();
            person.setName(Integer.toString(i / 2));
            requeryOperations.insert(person);
        }

        Result<Tuple> result = requeryOperations.select(Person.NAME).distinct().get();

        assertThat(result.toList()).hasSize(5);
    }

    @Test
    public void query_select_count() {
        Set<Person> people = RandomData.randomPeople(10);
        requeryOperations.insertAll(people);

        Result<Tuple> result = requeryOperations.select(Count.count(Person.class).as("bb")).get();
        assertThat(result.first().<Integer>get("bb")).isEqualTo(people.size());

        Result<Tuple> result2 = requeryOperations.select(Count.count(Person.class)).get();
        assertThat(result2.first().<Integer>get(0)).isEqualTo(people.size());

        assertThat(requeryOperations.count(Person.class).get().value()).isEqualTo(people.size());

        requeryOperations.count(Person.class).get().consume(
            count -> assertThat(count).isEqualTo(people.size())
        );
    }

    @Test
    public void query_select_count_where() {
        Person person = RandomData.randomPerson();
        person.setName("countMe");
        requeryOperations.insert(person);
        requeryOperations.insertAll(RandomData.randomPeople(9));

        assertThat(requeryOperations.count(Person.class).where(Person.NAME.eq("countMe")).get().value()).isEqualTo(1);

        Result<Tuple> result = requeryOperations.select(Count.count(Person.class).as("cnt"))
            .where(Person.NAME.eq("countMe"))
            .get();

        assertThat(result.first().<Integer>get("cnt")).isEqualTo(1);
    }

    @Test
    public void query_not_null() throws Exception {
        requeryOperations.insertAll(RandomData.randomPeople(10));

        Thread.sleep(10L);

        Result<Person> result = requeryOperations.select(Person.class).where(Person.NAME.notNull()).get();
        assertThat(result.toList()).hasSize(10);
    }

    @Test
    public void query_from_sub_query() {
        for (int i = 0; i < 10; i++) {
            Person person = RandomData.randomPerson();
            person.setAge(i + 1);
            requeryOperations.insert(person);
        }

        NumericAttribute<Person, Integer> personAge = Person.AGE;

        Return<? extends Result<Tuple>> subQuery = requeryOperations.select(personAge.sum().as("avg_age"))
            .from(Person.class)
            .groupBy(personAge)
            .as("sums");

        Result<Tuple> result =
            requeryOperations.select(NamedNumericExpression.ofInteger("avg_age").avg())
                .from(subQuery)
                .get();

        assertThat(result.first().<Integer>get(0)).isGreaterThanOrEqualTo(5);
    }

    @Test
    public void query_join_orderBy() {
        Person person = RandomData.randomPerson();
        person.setAddress(RandomData.randomAddress());
        requeryOperations.insert(person);

        // not a useful query just tests the sql output
        Result<Address> result = requeryOperations.select(Address.class)
            .join(Person.class).on(Person.ADDRESS_ID.eq(Address.ID))
            .where(Person.ID.eq(person.getId()))
            .orderBy(Address.CITY.desc())
            .get();

        List<Address> addresses = result.toList();
        assertThat(addresses.size()).isGreaterThan(0);
    }

    @Test
    public void query_select_min() {
        for (int i = 0; i < 9; i++) {
            requeryOperations.insert(RandomData.randomPerson());
        }
        Person person = RandomData.randomPerson();
        person.setBirthday(LocalDate.of(1800, 11, 11));
        requeryOperations.insert(person);

        Result<Tuple> query = requeryOperations.select(Person.BIRTHDAY.min().as("oldestBDay")).get();
        LocalDate birthday = query.first().get("oldestBDay");
        assertThat(birthday).isEqualTo(LocalDate.of(1800, 11, 11));
    }

    @Test
    public void query_select_trim() {
        Person person = RandomData.randomPerson();
        person.setName("  Name  ");
        requeryOperations.insert(person);

        Tuple result = requeryOperations.select(Person.NAME.trim().as("name")).get().first();
        String name = result.get(0);
        assertThat(name).isEqualTo("Name");
    }

    @Test
    public void query_select_substr() {
        Person person = RandomData.randomPerson();
        person.setName("  Name");
        requeryOperations.insert(person);

        Tuple result = requeryOperations.select(Person.NAME.substr(3, 6).as("name")).get().first();
        String name = result.get(0);
        assertThat(name).isEqualTo("Name");
    }

    @Test
    public void query_orderBy() {
        for (int i = 0; i < 10; i++) {
            Person person = RandomData.randomPerson();
            person.setAge(i);
            requeryOperations.insert(person);
        }

        Result<Tuple> query = requeryOperations.select(Person.AGE).orderBy(Person.AGE.desc()).get();

        int topAge = query.first().<Integer>get(0);
        assertThat(topAge).isEqualTo(9);
    }

    @Test
    public void query_orderBy_function() {
        Person person1 = RandomData.randomPerson();
        person1.setName("BOBB");
        requeryOperations.insert(person1);
        Person person2 = RandomData.randomPerson();
        person2.setName("BobA");
        requeryOperations.insert(person2);
        Person person3 = RandomData.randomPerson();
        person3.setName("bobC");
        requeryOperations.insert(person3);

        List<Tuple> people = requeryOperations.select(Person.NAME)
            .orderBy(Person.NAME.upper().desc())
            .get()
            .toList();

        assertThat(people).hasSize(3);
        assertThat(people.get(0).<String>get(0)).isEqualTo("bobC");
        assertThat(people.get(1).<String>get(0)).isEqualTo("BOBB");
        assertThat(people.get(2).<String>get(0)).isEqualTo("BobA");
    }

    @Test
    public void query_groupBy() {
        for (int i = 0; i < 5; i++) {
            Person person = RandomData.randomPerson();
            person.setAge(i);
            requeryOperations.insert(person);
        }

        Result<Tuple> result = requeryOperations.select(Person.AGE)
            .groupBy(Person.AGE)
            .having(Person.AGE.gt(3))
            .get();

        assertThat(result.toList()).hasSize(1);

        Result<Tuple> result2 = requeryOperations.select(Person.AGE)
            .groupBy(Person.AGE)
            .having(Person.AGE.lt(0))
            .get();

        assertThat(result2.toList()).isEmpty();
    }

    @Test
    public void query_select_where_in() {
        String name = "Hello!";
        Person person = RandomData.randomPerson();
        person.setName(name);
        requeryOperations.insert(person);

        Group group = new Group();
        group.setName(name);
        requeryOperations.insert(group);

        person.getGroups().add(group);
        requeryOperations.upsert(group);

        WhereAndOr<? extends Result<Tuple>> groupNames = requeryOperations.select(Group.NAME)
            .where(Group.NAME.eq(name));

        Person p = requeryOperations.select(Person.class)
            .where(Person.NAME.in(groupNames)).get().first();
        assertThat(p.getName()).isEqualTo(name);

        p = requeryOperations.select(Person.class)
            .where(Person.NAME.notIn(groupNames)).get().firstOrNull();
        assertThat(p).isNull();

        p = requeryOperations.select(Person.class)
            .where(Person.NAME.in(Arrays.asList("Hello!", "Other"))).get().first();
        assertThat(p.getName()).isEqualTo(name);

        p = requeryOperations.select(Person.class)
            .where(Person.NAME.in(Collections.singletonList("Hello!"))).get().first();
        assertThat(p.getName()).isEqualTo(name);

        p = requeryOperations.select(Person.class)
            .where(Person.NAME.notIn(Collections.singletonList("Hello!"))).get().firstOrNull();
        assertThat(p).isNull();
    }

    @Test
    public void query_between() {
        Person person = RandomData.randomPerson();
        person.setAge(75);
        requeryOperations.insert(person);

        Person p = requeryOperations.select(Person.class).where(Person.AGE.between(50, 100)).get().first();
        assertThat(p).isEqualTo(person);
    }

    @Test
    public void query_conditions() {
        Person person = RandomData.randomPerson();
        person.setAge(75);
        requeryOperations.insert(person);

        Person p = requeryOperations.select(Person.class).where(Person.AGE.gte(75)).get().first();
        assertThat(p).isEqualTo(person);

        p = requeryOperations.select(Person.class).where(Person.AGE.lte(75)).get().first();
        assertThat(p).isEqualTo(person);

        p = requeryOperations.select(Person.class).where(Person.AGE.gt(75)).get().firstOrNull();
        assertThat(p).isNull();

        p = requeryOperations.select(Person.class).where(Person.AGE.lt(75)).get().firstOrNull();
        assertThat(p).isNull();

        p = requeryOperations.select(Person.class).where(Person.AGE.ne(75)).get().firstOrNull();
        assertThat(p).isNull();
    }

    @Test
    public void query_compound_conditions() {
        Person person1 = RandomData.randomPerson();
        person1.setAge(75);
        requeryOperations.insert(person1);

        Person person2 = RandomData.randomPerson();
        person2.setAge(10);
        person2.setName("Carol");
        requeryOperations.insert(person2);

        Person person3 = RandomData.randomPerson();
        person3.setAge(0);
        person3.setName("Bob");
        requeryOperations.insert(person3);

        Result<Person> result = requeryOperations.select(Person.class)
            .where(Person.AGE.gt(5).and(Person.AGE.lt(75))).and(Person.NAME.ne("Bob"))
//            .where(Person.AGE.gt(5).and(Person.AGE.lt(75)).and(Person.NAME.ne("Bob"))) // TODO?: Error occurs
            .or(Person.NAME.eq("Bob"))
            .get();

        assertThat(result.toList()).hasSize(2).containsOnly(person2, person3);

        result = requeryOperations.select(Person.class)
            .where(Person.AGE.gt(5).or(Person.AGE.lt(75)))
            .and(Person.NAME.eq("Bob"))
            .get();

        assertThat(result.toList()).hasSize(1).containsOnly(person3);
    }

    @Test
    public void query_consume() {
        requeryOperations.insertAll(RandomData.randomPeople(10));

        List<Person> people = new ArrayList<>();
        Result<Person> result = requeryOperations.select(Person.class).get();
        result.each(people::add);

        assertThat(people).hasSize(10);
    }

    @Test
    public void query_map() {
        Person person = RandomData.randomPerson();
        person.setEmail("one@test.com");
        requeryOperations.insert(person);
        requeryOperations.insertAll(RandomData.randomPeople(9));

        Result<Person> result = requeryOperations.select(Person.class).get();
        Map<String, Person> map = result.toMap(Person.EMAIL, new ConcurrentHashMap<>());
        assertThat(map.get("one@test.com")).isNotNull();

        map = result.toMap(Person.EMAIL);
        assertThat(map.get("one@test.com")).isNotNull();

        Map<String, Person> jmap = result.toList().stream().collect(toMap(Person::getEmail, Function.identity()));
        assertThat(jmap.get("one@test.com")).isNotNull();
    }

    @Test
    public void query_update() {
        Person person = RandomData.randomPerson();
        person.setAge(100);
        requeryOperations.insert(person);

        int updatedCount = requeryOperations.update(Person.class)
            .set(Person.ABOUT, "nothing")
            .set(Person.AGE, 50)
            .where(Person.AGE.eq(100))
            .get()
            .value();

        assertThat(updatedCount).isEqualTo(1);
    }

    @Test
    public void query_update_refresh() {
        Person person = RandomData.randomPerson();
        person.setAge(100);
        requeryOperations.insert(person);

        int updatedCount = requeryOperations.update(Person.class)
            .set(Person.AGE, 50)
            .where(Person.ID.eq(person.getId()))
            .get()
            .value();

        assertThat(updatedCount).isEqualTo(1);

        Person selected = requeryOperations.select(Person.class).where(Person.ID.eq(person.getId())).get().first();
        assertThat(selected.getAge()).isEqualTo(50);
    }

    @Test
    public void query_coalesce() {
        Person person = RandomData.randomPerson();
        person.setName("Carol");
        person.setEmail(null);
        requeryOperations.insert(person);

        person = RandomData.randomPerson();
        person.setName("Bob");
        person.setEmail("test@test.com");
        person.setHomepage(null);
        requeryOperations.insert(person);

        Result<Tuple> result = requeryOperations.select(Coalesce.coalesce(Person.EMAIL, Person.NAME)).get();
        List<Tuple> list = result.toList();
        List<String> values = list.stream().map(it -> it.<String>get(0)).collect(Collectors.toList());

        assertThat(values).hasSize(2).containsOnly("Carol", "test@test.com");
    }

    @Test
    public void query_like() {
        Person person1 = RandomData.randomPerson();
        person1.setName("Carol");
        requeryOperations.insert(person1);
        Person person2 = RandomData.randomPerson();
        person2.setName("Bob");
        requeryOperations.insert(person2);

        Person p = requeryOperations.select(Person.class)
            .where(Person.NAME.like("B%"))
            .get()
            .first();

        assertThat(p.getName()).isEqualTo("Bob");

        p = requeryOperations.select(Person.class)
            .where(Person.NAME.lower().like("b%"))
            .get()
            .first();

        assertThat(p.getName()).isEqualTo("Bob");

        Person p2 = requeryOperations.select(Person.class)
            .where(Person.NAME.notLike("B%"))
            .get()
            .firstOrNull();

        assertThat(p2.getName()).isEqualTo("Carol");
    }

    @Test
    public void query_equals_ignore_case() {
        Person person = RandomData.randomPerson();
        person.setName("Carol");
        requeryOperations.insert(person);

        Person p = requeryOperations.select(Person.class)
            .where(Person.NAME.equalsIgnoreCase("carol"))
            .get()
            .first();

        assertThat(p).isEqualTo(person);
    }

    @Test
    public void query_case() {
        List<String> names = Arrays.asList("Carol", "Bob", "Jack");
        names.forEach(name -> {
            Person person = RandomData.randomPerson();
            person.setName(name);
            requeryOperations.insert(person);
        });

        Result<Tuple> a = requeryOperations.select(
            Person.NAME,
            Case.type(String.class)
                .when(Person.NAME.eq("Bob"), "B")
                .when(Person.NAME.eq("Carol"), "C")
                .elseThen("Unknown")
        )
            .from(Person.class)
            .orderBy(Person.NAME)
            .get();

        List<Tuple> list = a.toList();
        assertThat(list.get(0).<String>get(1)).isEqualTo("B");
        assertThat(list.get(1).<String>get(1)).isEqualTo("C");
        assertThat(list.get(2).<String>get(1)).isEqualTo("Unknown");

        a = requeryOperations.select(
            Person.NAME,
            Case.type(Integer.class)
                .when(Person.NAME.eq("Bob"), 1)
                .when(Person.NAME.eq("Carol"), 2)
                .elseThen(0)
        )
            .orderBy(Person.NAME)
            .get();

        list = a.toList();
        assertThat(list.get(0).<Integer>get(1)).isEqualTo(1);
        assertThat(list.get(1).<Integer>get(1)).isEqualTo(2);
        assertThat(list.get(2).<Integer>get(1)).isEqualTo(0);
    }

    @Test
    public void query_union() {
        Person person = RandomData.randomPerson();
        person.setName("Carol");
        requeryOperations.insert(person);

        Group group = new Group();
        group.setName("Hello!");
        requeryOperations.insert(group);

        // select name as name from FuncPerson
        // union
        // select name as name from FuncGroup order by name
        List<Tuple> result = requeryOperations.select(Person.NAME.as("name"))
            .union()
            .select(Group.NAME.as("name"))
            .orderBy(Group.NAME.as("name"))
            .get()
            .toList();

        assertThat(result.get(0).<String>get(0)).isEqualTo("Carol");
        assertThat(result.get(1).<String>get(0)).isEqualTo("Hello!");
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void query_raw() {
        int count = 5;

        List<Person> people = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            people.add(requeryOperations.insert(RandomData.randomPerson()));
        }

        List<Long> resultIds = new ArrayList<>();

        Result<Tuple> result = requeryOperations.raw("select * from Person");
        List<Tuple> rows = result.toList();
        assertThat(rows).hasSize(count);

        for (int index = 0; index < rows.size(); index++) {
            Tuple row = rows.get(index);
            String name = row.get("name");
            assertThat(name).isEqualTo(people.get(index).getName());
            Long id = row.<Long>get("personId");
            assertThat(id).isEqualTo(people.get(index).getId());
            resultIds.add(id);
        }

        result = requeryOperations.raw("select * from Person WHERE personId in ?", resultIds);
        rows = result.toList();
        List<Long> ids = rows.stream().map(it -> it.<Long>get("personId")).collect(Collectors.toList());
        assertThat(ids).isEqualTo(resultIds);

        result = requeryOperations.raw("select count(*) from Person");
        int number = result.first().<Number>get(0).intValue();
        assertThat(number).isEqualTo(count);

        result = requeryOperations.raw("select * from Person WHERE personId = ?", people.get(0));
        assertThat(result.first().<Long>get("personId")).isEqualTo(people.get(0).getId());

    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void query_raw_entities() {
        int count = 5;

        List<Person> people = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            people.add(requeryOperations.insert(RandomData.randomPerson()));
        }

        List<Long> resultIds = new ArrayList<>();

        Result<Person> result = requeryOperations.raw(Person.class, "select * from Person");
        List<Person> rows = result.toList();
        assertThat(rows).hasSize(count);

        for (int index = 0; index < rows.size(); index++) {
            Person row = rows.get(index);
            String name = row.getName();
            assertThat(name).isEqualTo(people.get(index).getName());
            Long id = row.getId();
            assertThat(id).isEqualTo(people.get(index).getId());
            resultIds.add(id);
        }

        result = requeryOperations.raw(Person.class, "select * from Person WHERE personId in ?", resultIds);
        rows = result.toList();
        List<Long> ids = rows.stream().map(Person::getId).collect(Collectors.toList());
        assertThat(ids).isEqualTo(resultIds);

        result = requeryOperations.raw(Person.class, "select * from Person WHERE personId = ?", people.get(0));
        assertThat(result.first().getId()).isEqualTo(people.get(0).getId());
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void query_raw_paging() {
        int count = 5;
        for (int i = 0; i < count; i++) {
            requeryOperations.insert(RandomData.randomPerson());
        }

        long totals = requeryOperations.raw("select count(*) from Person").first().get(0);

        Result<Person> result = requeryOperations.raw(Person.class, "select * from Person");
        List<Person> rows = result.toList();

        long totals2 = requeryOperations.raw("select count(*) from Person").first().get(0);

        Result<Person> result2 = requeryOperations.raw(Person.class, "select * from Person");
        List<Person> rows2 = result2.toList();

        log.debug("rows size={}, rows2 size={}, totals={}, totals2={}", rows.size(), rows2.size(), totals, totals2);

        assertThat(rows.size()).isEqualTo(totals);
        assertThat(rows2.size()).isEqualTo(totals2);
    }

    @Test
    public void query_union_join_on_same_entities() {
        Group group = new Group();
        group.setName("Hello!");
        requeryOperations.insert(group);

        Person person1 = RandomData.randomPerson();
        person1.setName("Carol");
        person1.getGroups().add(group);
        requeryOperations.insert(person1);

        Person person2 = RandomData.randomPerson();
        person2.setName("Bob");
        person2.getGroups().add(group);
        requeryOperations.insert(person2);

        Expression[] columns = { Person.NAME.as("personName"), Group.NAME.as("GroupName") };
        List<Tuple> rows = requeryOperations.select(columns).where(Person.ID.eq(person1.getId()))
            .union()
            .select(columns).where(Person.ID.eq(person2.getId()))
            .orderBy(Person.NAME.as("personName"))
            .get()
            .toList();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).<String>get("personName")).isEqualTo("Bob");
        assertThat(rows.get(0).<String>get("groupName")).isEqualTo("Hello!");
        assertThat(rows.get(1).<String>get("personName")).isEqualTo("Carol");
        assertThat(rows.get(1).<String>get("groupName")).isEqualTo("Hello!");
    }

    @Test
    public void violate_unique_constraint() {
        assertThatThrownBy(() -> {
            UUID uuid = UUID.randomUUID();
            Person p1 = RandomData.randomPerson();
            p1.setUUID(uuid);
            requeryOperations.insert(p1);

            Person p2 = RandomData.randomPerson();
            p2.setUUID(uuid);
            requeryOperations.insert(p2);
        }).isInstanceOf(PersistenceException.class);
    }
}
