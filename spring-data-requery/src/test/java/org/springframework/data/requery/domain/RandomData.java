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

package org.springframework.data.requery.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.requery.domain.basic.BasicGroup;
import org.springframework.data.requery.domain.basic.BasicLocation;
import org.springframework.data.requery.domain.basic.BasicUser;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * RandomData
 *
 * @author debop
 * @since 18. 6. 5
 */
@Slf4j
public class RandomData {

    private RandomData() {}

    private static Random rnd = new Random(System.currentTimeMillis());

    private static String[] firstNames = new String[] { "Alice", "Bob", "Carol", "Debop" };
    private static String[] lastNames = new String[] { "Smith", "Lee", "Jones", "Bae" };

    public static BasicUser randomUser() {
        try {
            BasicUser user = new BasicUser();
            user.setName(firstNames[rnd.nextInt(firstNames.length)] + " " + lastNames[rnd.nextInt(lastNames.length)]);
            user.setEmail(user.getName().replace(" ", ".").toLowerCase() + "@example.com");
            user.setUuid(UUID.randomUUID());
            user.setHomepage(new URL("http://www.coupang.com"));
            user.setBirthday(LocalDate.of(1900 + rnd.nextInt(90), rnd.nextInt(11) + 1, rnd.nextInt(27) + 1));

            return user;
        } catch (Exception e) {
            log.warn("Error when create person.", e);
            return new BasicUser();
        }
    }

    public static Set<BasicUser> randomUsers(int count) {
        Set<BasicUser> users = new HashSet<>();
        int userCount = 0;
        while (userCount < count) {
            BasicUser user = randomUser();
            if (users.add(user)) {
                userCount++;
            }
        }
        return users;
    }

    public static BasicLocation randomLocation() {
        BasicLocation location = new BasicLocation();

        location.setLine1(rnd.nextInt(4) + " Fake St.");
        location.setCity("Seoul");
        location.setState("Seoul");
        location.setCountryCode("KR");
        location.setZip(String.valueOf(10000 + rnd.nextInt(89999)));

        return location;
    }

    public static BasicGroup randomBasicGroup() {
        BasicGroup group = new BasicGroup();
        group.setName("group " + rnd.nextInt(1000));
        group.setDescription("description " + rnd.nextInt(1000));
        return group;
    }
}
