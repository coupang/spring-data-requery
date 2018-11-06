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

package requery.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import requery.demo.domain.City;
import requery.demo.repository.CityRepository;
import requery.demo.services.CityService;

/**
 * CityController
 *
 * @author debop
 * @since 18. 10. 31
 */
@RestController
public class CityController {

    private final CityService cityService;

    private final CityRepository cityRepo;

    @Autowired
    public CityController(CityService cityService, CityRepository cityRepo) {
        this.cityService = cityService;
        this.cityRepo = cityRepo;
    }


    @GetMapping("/city")
    public Flux<City> findAll() {
        return Flux.fromIterable(cityService.findAll());
    }

    @GetMapping("/city/{cityId}")
    public City findById(@PathVariable("cityId") Long cityId) {
        City city = cityRepo.findByIdAndDeletedFalse(cityId);
        if (city == null) {
            city = City.of("Seoul", "Korean");
            cityRepo.save(city);
        }
        return city;
    }
}
