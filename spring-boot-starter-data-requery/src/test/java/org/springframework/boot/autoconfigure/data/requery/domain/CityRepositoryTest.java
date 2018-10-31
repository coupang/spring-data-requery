package org.springframework.boot.autoconfigure.data.requery.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.requery.configs.TestRequeryConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CityRepositoryTest
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestRequeryConfiguration.class })
@EnableRequeryRepositories(basePackageClasses = { CityRepository.class })
public class CityRepositoryTest {

    @Autowired
    private CityRepository repository;

    @Test
    public void contextLoading() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void testFindByIdAndDeletedFalse() {
        City city = new City("Seoul", "Korea");
        repository.save(city);

        assertThat(city.getId()).isNotNull();

        City active = repository.findByIdAndDeletedFalse(city.getId());
        assertThat(active).isNotNull();

        city.setDeleted(true);
        repository.save(city);

        City deleted = repository.findByIdAndDeletedFalse(city.getId());
        assertThat(deleted).isNull();
    }
}
