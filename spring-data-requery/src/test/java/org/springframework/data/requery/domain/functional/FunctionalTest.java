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

import io.requery.meta.NumericAttribute;
import io.requery.proxy.CompositeKey;
import io.requery.proxy.EntityProxy;
import io.requery.proxy.PropertyState;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;
import org.springframework.data.requery.domain.EntityState;
import org.springframework.data.requery.domain.model.AbstractAddress;
import org.springframework.data.requery.domain.model.AbstractPerson;
import org.springframework.data.requery.domain.model.AbstractPhone;
import org.springframework.data.requery.domain.model.Address;
import org.springframework.data.requery.domain.model.Func_Group_Members;
import org.springframework.data.requery.domain.model.Group;
import org.springframework.data.requery.domain.model.GroupType;
import org.springframework.data.requery.domain.model.Person;
import org.springframework.data.requery.domain.model.Phone;
import org.springframework.data.requery.domain.model.RandomData;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Diego on 2018. 6. 12..
 */
@Slf4j
public class FunctionalTest extends AbstractDomainTest {

    private static final int COUNT = 30;

    @Before
    public void setup() {
        requeryOperations.deleteAll(Address.class);
        requeryOperations.deleteAll(Group.class);
        requeryOperations.deleteAll(Phone.class);
        requeryOperations.deleteAll(Person.class);
    }

    @Test
    public void equals_and_hashCode() {
        Person p1 = new Person();
        p1.setAge(10);
        p1.setName("Bob");
        p1.setEmail("bob@example.com");

        Person p2 = new Person();
        p2.setAge(10);
        p2.setName("Bob");
        p2.setEmail("bob@example.com");

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    public void copyable_entity() {
        Address addr = new Address();
        addr.setCity("Sanfrancisco");
        addr.setState("CA");
        addr.setCountryCode("US");
        addr.setZip("12345");

        Address copied = addr.copy();
        assertThat(copied).isEqualTo(addr);
    }

    @Test
    public void custom_converter() {
        Phone phone = RandomData.randomPhone();

        requeryOperations.insert(phone);

        Phone loaded = requeryOperations.select(Phone.class)
            .where(Phone.EXTENSIONS.eq(phone.getExtensions()))
            .get()
            .first();

        assertThat(loaded).isEqualTo(phone);
    }

    @Test
    public void select_by_id() {
        Person person = RandomData.randomPerson();

        requeryOperations.insert(person);
        assertThat(person.getId()).isGreaterThan(0L);

        Person loaded = requeryOperations.findById(Person.class, person.getId());
        assertThat(loaded).isEqualTo(person);

        Person loaded2 = requeryOperations.select(Person.class)
            .where(Person.ID.eq(person.getId()))
            .get()
            .firstOrNull();

        assertThat(loaded2).isEqualTo(loaded);
    }

    @Test
    public void insert_with_default_value() {
        Person person = RandomData.randomPerson();

        requeryOperations.insert(person);
        assertThat(person.getId()).isGreaterThan(0L);

        Person loaded = requeryOperations.findById(Person.class, person.getId());
        assertThat(loaded).isEqualTo(person);
        assertThat(loaded.getDescription()).isEqualTo("empty");
    }

    @Test
    public void insert_select_null_key_reference() {
        requeryOperations.deleteAll(Person.class);

        Person person = RandomData.randomPerson();

        requeryOperations.insert(person);
        assertThat(person.getId()).isGreaterThan(0L);
        assertThat(person.getAddress()).isNull();

        AbstractAddress address = requeryOperations.select(Person.class)
            .get()
            .first().getAddress();
        assertThat(address).isNull();
    }

    @Test
    public void insert_many_people_with_transaction() {
        requeryOperations.withTransaction(entityStore -> {
            IntStream.range(0, COUNT).forEach(it -> {
                Person person = RandomData.randomPerson();
                entityStore.insert(person);
            });

            int personCount = entityStore.count(Person.class).get().value();
            return assertThat(personCount).isEqualTo(COUNT);
        });
    }

    @Test
    public void insert_many_people_with_batch() throws Exception {
        Set<Person> people = RandomData.randomPeople(COUNT);
        // Batch 방식으로 저장한다.
        requeryOperations.insertAll(people);

        Thread.sleep(100);

        int personCount = requeryOperations.count(Person.class).get().value();
        assertThat(personCount).isEqualTo(COUNT);
    }

    @Test
    public void insert_many_people_with_multi_thread() throws Exception {
        // HINT: Executor를 이용하여 비동기 방식으로 데이터 작업하기
        // HINT: Coroutine 을 이용하는 것이 더 낫다 (Transaction 도 되고)

        ForkJoinPool executor = ForkJoinPool.commonPool();

        try {
            CountDownLatch latch = new CountDownLatch(COUNT);
            Map<Long, Person> map = new ConcurrentHashMap<>();

            IntStream.range(0, COUNT).forEach(
                it -> executor.submit(() -> {
                    Person person = RandomData.randomPerson();
                    requeryOperations.insert(person);
                    assertThat(person.getId()).isGreaterThan(0L);
                    map.put(person.getId(), person);
                    latch.countDown();
                }));
            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();

            map.forEach((key, value) -> {
                Person loaded = requeryOperations.findById(Person.class, key);
                assertThat(loaded).isNotNull();
                assertThat(loaded).isEqualTo(value);
            });
        } finally {
            executor.shutdown();
        }
    }

//    @Test
//    public void insert_many_people_with_coroutines() { }

    @Test
    public void insert_empty_entity() {
        Phone phone = new Phone();
        requeryOperations.insert(phone);
        assertThat(phone.getId()).isNotNull();
    }

    private class DerivedPhone extends Phone {
        @Nonnull
        @Override
        public String toString() {
            return "DerivedPhone";
        }
    }

    @Test
    public void insert_derived_class() {
        DerivedPhone derivedPhone = new DerivedPhone();
        derivedPhone.setPhoneNumber("555-5555");

        requeryOperations.insert(derivedPhone);
        assertThat(derivedPhone.getId()).isNotNull();

        Phone loaded = requeryOperations.findById(Phone.class, derivedPhone.getId());
        assertThat(loaded).isNotNull();

        // NOTE: 하지만 FuncPhone 을 DerivedPhone으로 casting 은 하지 못한다 !!!
    }

    @Test
    public void insert_by_query() {
        // requeryTemplate말고 dataStore를 써야한다.
        Long id1 = dataStore.insert(Person.class)
            .value(Person.ABOUT, "nothing")
            .value(Person.AGE, 50)
            .get()
            .first()
            .get(Person.ID);

        assertThat(id1).isNotNull().isGreaterThan(0L);

        Long id2 = dataStore.insert(Person.class)
            .value(Person.NAME, "Bob")
            .value(Person.AGE, 50)
            .get()
            .first()
            .get(Person.ID);

        assertThat(id2).isNotNull().isGreaterThan(0).isNotEqualTo(id1);
    }

    @Test
    public void insert_into_select_query() {
        Group group = new Group();
        group.setName("Bob");
        group.setDescription("Bob's group");

        requeryOperations.insert(group);


        int count = dataStore.insert(Person.class, Person.NAME, Person.DESCRIPTION)
            .query(dataStore.select(Group.NAME, Group.DESCRIPTION))
            .get()
            .first()
            .count();

        assertThat(count).isEqualTo(1);

        Person person = requeryOperations.select(Person.class).get().first();
        assertThat(person.getName()).isEqualTo(group.getName());
        assertThat(person.getDescription()).isEqualTo(group.getDescription());
    }

    @Test
    public void insert_with_transaction_callable() {
        String result = requeryOperations.withTransaction(entityStore -> {
            IntStream.range(0, COUNT).forEach(it -> {
                Person person = RandomData.randomPerson();
                entityStore.insert(person);
                assertThat(person.getId()).isNotNull();
            });
            return "success";
        });
        assertThat(result).isEqualTo("success");
    }

    @Test
    public void find_by_composite_key() {
        Group group = new Group();
        group.setName("group");
        group.setType(GroupType.PRIVATE);

        Person person = RandomData.randomPerson();
        person.getGroups().add(group);

        requeryOperations.insert(person);
        assertThat(person.getId()).isNotNull();

        Map<NumericAttribute<Func_Group_Members, Long>, Long> map = new HashMap<>();
        map.put(Func_Group_Members.GROUPS_ID, group.getId());
        map.put(Func_Group_Members.PERSON_ID, person.getId());

        CompositeKey<Func_Group_Members> compositeKey = new CompositeKey(map);

        Func_Group_Members joined = requeryOperations.findById(Func_Group_Members.class, compositeKey);
        assertThat(joined.getPersonId()).isEqualTo(person.getId());
        assertThat(joined.getGroupsId()).isEqualTo(group.getId());
    }

    @Test
    public void find_by_key_and_delete() {
        Person person = RandomData.randomPerson();
        Address address = RandomData.randomAddress();

        person.setAddress(address);

        requeryOperations.insert(person);
        assertThat(person.getId()).isNotNull();

        Person loaded = requeryOperations.findById(Person.class, person.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getAddress()).isEqualTo(address);

        requeryOperations.delete(loaded);

        loaded = requeryOperations.findById(Person.class, person.getId());
        assertThat(loaded).isNull();
    }

    @Test
    public void find_by_key_and_delete_inverse() {
        Person person = RandomData.randomPerson();
        Address address = RandomData.randomAddress();

        person.setAddress(address);

        requeryOperations.insert(person);
        assertThat(person.getId()).isNotNull();

        requeryOperations.delete(person);

        Person loaded = requeryOperations.findById(Person.class, person.getId());
        assertThat(loaded).isNull();
        assertThat(requeryOperations.count(Address.class).get().value()).isEqualTo(0);
    }

    @Test
    public void rollback_transaction() {
        List<Long> ids = new ArrayList<>();

        try {
            requeryOperations.withTransaction(entityStore -> {
                IntStream.range(0, COUNT).forEach(it -> {
                    Person person = RandomData.randomPerson();
                    entityStore.insert(person);
                    assertThat(person.getId()).isNotNull();
                    ids.add(person.getId());

                    if (it == 5) {
                        throw new RuntimeException("rollback ...");
                    }
                });
                return "success";
            });
        } catch (Exception ignored) {
            log.info("Rollback executed.");
        }

        assertThat(requeryOperations.count(Person.class).get().value()).isEqualTo(0);
    }

    @Test
    public void check_changed_attribute() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);
        assertThat(person.getId()).isNotNull();

        person.setName("Bob Smith");
        person.setBirthday(LocalDate.of(1983, 11, 14));

        // HINT: Proxy를 통해 entity의 변화관리를 조회활 수 있다.
        EntityProxy<Person> proxy = Person.$TYPE.getProxyProvider().apply(person);
        AtomicInteger count = new AtomicInteger();
        Person.$TYPE.getAttributes().forEach(attr -> {
            if (proxy.getState(attr).equals(PropertyState.MODIFIED)) {
                log.debug("modified attr={}", attr.getName());
                // lambda 내에서 값을 변경하기 때문에 AtomicInteger를 사용했다.
                count.getAndIncrement();
            }
        });
        assertThat(count.get()).isEqualTo(2);
        requeryOperations.update(person);

        Person.$TYPE.getAttributes().forEach(attr -> {
            if (proxy.getState(attr).equals(PropertyState.MODIFIED)) {
                fail("변경된 것이 없어야 합니다.");
            }
        });
    }

    @Test
    public void update_no_changed_entity() {
        Person person = RandomData.randomPerson();

        requeryOperations.insert(person);
        assertThat(person.getId()).isNotNull();

        requeryOperations.update(person);
    }

    @Test
    public void entity_listener() {
        Person person = RandomData.randomPerson();

        requeryOperations.insert(person);
        assertThat(person.getPrevious()).isEqualTo(EntityState.PRE_SAVE);
        assertThat(person.getCurrent()).isEqualTo(EntityState.POST_SAVE);

        person.setEmail("bob@example.com");
        requeryOperations.update(person);
        assertThat(person.getPrevious()).isEqualTo(EntityState.PRE_UPDATE);
        assertThat(person.getCurrent()).isEqualTo(EntityState.POST_UPDATE);

        requeryOperations.delete(person);
        assertThat(person.getPrevious()).isEqualTo(EntityState.PRE_DELETE);
        assertThat(person.getCurrent()).isEqualTo(EntityState.POST_DELETE);
    }

    @Test
    public void insert_one_to_one_entity_with_null_association() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);
        assertThat(person.getAddress()).isNull();
    }

    @Test
    public void insert_one_to_one_entity_with_null_association_inverse() {
        Address address = RandomData.randomAddress();
        requeryOperations.insert(address);
        assertThat(address.getPerson()).isNull();
    }

    @Test
    public void insert_one_to_one() {
        Address address = RandomData.randomAddress();
        requeryOperations.insert(address);
        assertThat(address.getId()).isNotNull();

        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);
        assertThat(person.getId()).isNotNull();

        person.setAddress(address);
        requeryOperations.update(person);

        // fetch inverse
        assertThat(address.getPerson()).isEqualTo(person);

        // unset
        person.setAddress(null);
        requeryOperations.update(person);

        // HINT: address는 update하지 않았지만 association에 변화를 적용하려면 refresh를 수행해야 한다
        requeryOperations.refresh(address);
        assertThat(address.getPerson()).isNull();

        Person loaded = requeryOperations.findById(Person.class, person.getId());
        assertThat(loaded.getAddress()).isNull();
    }

    @Test
    public void insert_one_to_one_with_cascade() {
        Address address = RandomData.randomAddress();
        Person person = RandomData.randomPerson();
        person.setAddress(address);

        requeryOperations.insert(person);

        // HINT: JPA는 양방향에 대해 모두 설정해주어야 하지만, requery는 insert 시에 cascade 해준다
        assertThat(address.getPerson()).isEqualTo(person);

        Address loadedAddr = requeryOperations.findById(Address.class, address.getId());
        assertThat(loadedAddr.getPerson()).isEqualTo(person);
    }

    @Test
    public void refresh_all() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone);

        requeryOperations.update(person);
        requeryOperations.refreshAllProperties(person);

        assertThat(person.getPhoneNumberList()).contains(phone);
    }

    @Test
    public void refresh_multiple() {
        List<Person> people = IntStream
            .range(0, 10)
            .mapToObj(i -> RandomData.randomPerson())
            .collect(Collectors.toList());
        requeryOperations.insertAll(people);

        int count = requeryOperations.update().set(Person.NAME, "fff").get().value();
        assertThat(count).isEqualTo(people.size());

        // refreshAlL 을 각각의 entity에 대해서 호출해줘야 제대로 반영된다.
        // refreshAllProperties(people)
        // refresh(people, FuncPerson.NAME)

        people.forEach(person -> {
            requeryOperations.refresh(person, Person.NAME);
            log.debug("person name={}", person.getName());
        });
        assertThat(people.stream().allMatch(it -> it.getName().equals("fff"))).isTrue();
    }

    @Test
    public void refresh_attributes() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone);

        requeryOperations.update(person);
        requeryOperations.refresh(person, Person.NAME, Person.PHONE_NUMBER_LIST, Person.ADDRESS, Person.EMAIL);

        assertThat(person.getPhoneNumberSet()).contains(phone);
    }

    @Test
    public void version_increment() {
        Group group = new Group();
        group.setName("group");
        group.setType(GroupType.PRIVATE);
        requeryOperations.insert(group);

        group.setType(GroupType.PUBLIC);
        requeryOperations.update(group);
        requeryOperations.refresh(group, Group.VERSION);

        log.debug("Group version={}", group.getVersion());
        assertThat(group.getVersion()).isGreaterThan(0);

        Group group2 = new Group();
        group2.setName("group2");
        group2.setType(GroupType.PRIVATE);

        requeryOperations.insert(group2);
        requeryOperations.refresh(Arrays.asList(group, group2), Group.VERSION);

        log.debug("Group version={}", group.getVersion());
        log.debug("Group2 version={}", group2.getVersion());
    }

    @Test
    public void version_update() {
        Group group = new Group();
        group.setName("Test1");
        requeryOperations.upsert(group);
        assertThat(group.getVersion()).isGreaterThan(0);

        group.setName("Test2");
        requeryOperations.upsert(group);
        assertThat(group.getVersion()).isGreaterThan(1);

        group.setName("Test3");
        requeryOperations.upsert(group);
        assertThat(group.getVersion()).isGreaterThan(2);
    }

    @Test
    public void fill_result_by_collect() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Set<Person> people = requeryOperations.select(Person.class)
            .get()
            .collect(new HashSet<>());
        assertThat(people).hasSize(1);
    }

    @Test
    public void result_to_list() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        List<Person> people = requeryOperations.select(Person.class)
            .get()
            .toList();
        assertThat(people).hasSize(1);
    }

    @Test
    public void delete_cascade_one_to_one() {
        Address address = RandomData.randomAddress();
        requeryOperations.insert(address);

        assertThat(address.getId()).isNotNull();
        int addressId = address.getId();

        Person person = RandomData.randomPerson();
        person.setAddress(address);

        requeryOperations.insert(person);
        requeryOperations.delete(person);

        assertThat(address.getPerson()).isNull();
        assertThat(requeryOperations.findById(Address.class, addressId)).isNull();
    }

    @Test
    public void delete_one() {
        Person person = RandomData.randomPerson();

        requeryOperations.insert(person);
        assertThat(person.getId()).isNotNull();

        requeryOperations.delete(person);
        assertThat(requeryOperations.findById(Person.class, person.getId())).isNull();
    }

    @Test
    public void delete_cascade_remove_one_to_many() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        phone1.setOwner(person);
        phone2.setOwner(person);

        requeryOperations.insert(phone1);
        requeryOperations.insert(phone2);
        requeryOperations.refresh(person);

        assertThat(person.getPhoneNumberList()).containsOnly(phone1, phone2);

        requeryOperations.delete(phone1);
        assertThat(person.getPhoneNumberList()).containsOnly(phone2);
    }

    @Test
    public void delete_cascade_one_to_many() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone1 = RandomData.randomPhone();
        phone1.setOwner(person);
        requeryOperations.insert(phone1);

        Integer phoneId = phone1.getId();

        assertThat(person.getPhoneNumbers()).hasSize(1);
        requeryOperations.delete(person);

        assertThat(requeryOperations.findById(Phone.class, phoneId)).isNull();
    }

    @Test
    public void delete_one_to_many_result() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone1 = RandomData.randomPhone();
        phone1.setOwner(person);
        Phone phone2 = RandomData.randomPhone();
        phone2.setOwner(person);
        requeryOperations.insertAll(Arrays.asList(phone1, phone2));
        requeryOperations.refresh(person);

        assertThat(person.getPhoneNumbers().toList()).hasSize(2);

        requeryOperations.deleteAll(person.getPhoneNumbers());

        assertThat(requeryOperations.count(Phone.class).get().value()).isEqualTo(0);
    }

    @Test
    public void insert_one_to_many() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone1 = RandomData.randomPhone();
        phone1.setOwner(person);
        Phone phone2 = RandomData.randomPhone();
        phone2.setOwner(person);
        requeryOperations.insertAll(Arrays.asList(phone1, phone2));

        // both works
        Set<AbstractPhone> set = person.getPhoneNumbers()
            .stream()
            .collect(Collectors.toSet());
        assertThat(set).hasSize(2).containsOnly(phone1, phone2);

    }

    @Test
    public void insert_one_to_many_inverse_update() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone1);
        person.getPhoneNumbers().add(phone2);

        requeryOperations.update(person);

        Set<AbstractPhone> set = person.getPhoneNumbers().stream().collect(Collectors.toSet());
        assertThat(set).hasSize(2).containsOnly(phone1, phone2);
        assertThat(phone1.getOwner()).isEqualTo(person);
        assertThat(phone2.getOwner()).isEqualTo(person);
    }

    @Test
    public void insert_one_to_many_inverse() {
        Person person = RandomData.randomPerson();
        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        phone1.setOwner(person);
        person.getPhoneNumbers().add(phone1);
        person.getPhoneNumbers().add(phone2);

        requeryOperations.insert(person);

        Set<AbstractPhone> set = person.getPhoneNumbers().stream().collect(Collectors.toSet());
        assertThat(set).hasSize(2).containsOnly(phone1, phone2);
        assertThat(phone1.getOwner()).isEqualTo(person);
        assertThat(phone2.getOwner()).isEqualTo(person);
    }

    @Test
    public void insert_one_to_many_inverse_through_set() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone1);
        person.getPhoneNumbers().add(phone2);

        requeryOperations.update(person);

        assertThat(person.getPhoneNumbers()).hasSize(2).containsOnly(phone1, phone2);
    }

    @Test
    public void insert_one_to_many_insert() {
        Person person = RandomData.randomPerson();
        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone1);
        person.getPhoneNumbers().add(phone2);

        requeryOperations.insert(person);
        Set<AbstractPhone> set = person.getPhoneNumbers().stream().collect(Collectors.toSet());
        assertThat(set).hasSize(2).containsOnly(phone1, phone2);
        assertThat(requeryOperations.count(Phone.class).get().value()).isEqualTo(2);
    }

    @Test
    public void insert_one_to_many_insert_through_list() {
        Person person = RandomData.randomPerson();
        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone1);
        person.getPhoneNumbers().add(phone2);

        requeryOperations.insert(person);

        Set<AbstractPhone> set = person.getPhoneNumberList().stream().collect(Collectors.toSet());
        assertThat(set).hasSize(2).containsOnly(phone1, phone2);
    }

    @Test
    public void many_to_one_refresh() {
        Person person = RandomData.randomPerson();
        Phone phone1 = RandomData.randomPhone();
        Phone phone2 = RandomData.randomPhone();
        person.getPhoneNumbers().add(phone1);
        person.getPhoneNumbers().add(phone2);

        requeryOperations.insert(person);
        assertThat(phone1.getOwner()).isEqualTo(person);
        assertThat(phone2.getOwner()).isEqualTo(person);

        requeryOperations.refresh(phone1, Phone.OWNER);
        requeryOperations.refresh(phone2, Phone.OWNER);
    }

    @Test
    public void insert_many_to_many() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);
        assertThat(person.getGroups().toList()).isEmpty();

        List<Group> addedGroups = new ArrayList<>();

        requeryOperations.withTransaction(entityStore -> {
            IntStream.range(0, 10).forEach(it -> {
                Group group = new Group();
                group.setName("Group" + it);
                group.setDescription("Some description");
                group.setType(GroupType.PRIVATE);
                entityStore.insert(group);
                person.getGroups().add(group);
                addedGroups.add(group);
            });
            return entityStore.update(person);
        });

        requeryOperations.refresh(person, Person.GROUPS);

        addedGroups.forEach(group -> assertThat(group.getMembers().toList()).contains(person));
    }

    @Test
    public void insert_many_to_many_self_referencing() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        List<Person> addedPeople = new ArrayList<>();
        IntStream.range(0, 10).forEach(it -> {
            Person p = RandomData.randomPerson();
            person.getFriends().add(p);
            addedPeople.add(p);
        });

        requeryOperations.update(person);

        assertThat(person.getFriends()).containsAll(addedPeople);
        assertThat(requeryOperations.count(Person.class).get().value()).isEqualTo(11);
    }

    @Test
    public void iterate_insert_many() {
        Person person = RandomData.randomPerson();
        assertThat(person.getGroups()).isEmpty();

        Set<Group> toAdd = new HashSet<>();

        IntStream.range(0, 10).forEach(it -> {
            Group group = new Group();
            group.setName("Group" + it);
            person.getGroups().add(group);
            toAdd.add(group);
        });

        AtomicInteger count = new AtomicInteger();
        person.getGroups().forEach(it -> {
            assertThat(toAdd.contains(it)).isTrue();
            count.getAndIncrement();
        });
        assertThat(count.get()).isEqualTo(10);
        requeryOperations.insert(person);
    }

    @Test
    public void delete_many_to_many() {
        Person person = RandomData.randomPerson();
        requeryOperations.insert(person);

        Set<Group> groups = new HashSet<>();

        requeryOperations.withTransaction(entityStore -> {
            IntStream.range(0, 10).forEach(it -> {
                Group group = new Group();
                group.setName("DeleteGroup" + it);
                entityStore.insert(group);
                person.getGroups().add(group);
                groups.add(group);
            });
            return entityStore.update(person);
        });
        groups.forEach(it -> person.getGroups().remove(it));
        requeryOperations.update(person);
        assertThat(person.getGroups().toList()).isEmpty();

        // many to many 관계를 끊은 것이므로 group 은 삭제되지 않는다.
        assertThat(requeryOperations.count(Group.class).get().value()).isEqualTo(10);
    }

    @Test
    public void many_to_many_order_by() {
        Group group = new Group();
        group.setName("Group");
        requeryOperations.insert(group);

        IntStream.iterate(3, i -> i - 1).limit(4).forEach(it -> {
            Person person = RandomData.randomPerson();
            char c = (char) (65 + it);
            person.setName(Character.toString(c));
            requeryOperations.insert(person);
            group.getOwners().add(person);
        });

        requeryOperations.update(group);
        requeryOperations.refresh(group, Group.OWNERS);

        List<AbstractPerson> owners = group.getOwners().toList();

        assertThat(owners).hasSize(4);
        assertThat(owners.get(0).getName()).isEqualTo("A");
        assertThat(owners.get(1).getName()).isEqualTo("B");
        assertThat(owners.get(2).getName()).isEqualTo("C");
        assertThat(owners.get(3).getName()).isEqualTo("D");
    }
}
