/*
 * Copyright 2025 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.osmerion.omittable.spring.boot.webflux;

import com.example.Main;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureWebTestClient
@SpringBootTest(
    classes = Main.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public final class SpringWebFluxIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testHandlerMethodArgumentResolver() {
        assertThat(this.testRestTemplate.getForObject("http://localhost:" + port + "/person?required=Karl", String.class))
            .isEqualTo("Karl, Omittable.absent");

        assertThat(this.testRestTemplate.getForObject("http://localhost:" + port + "/person?required=Karl&omittable=d3a33656-3fb4-4430-8103-b7c60f018eb4", String.class))
            .isEqualTo("Karl, Omittable[d3a33656-3fb4-4430-8103-b7c60f018eb4]");

        assertThat(this.testRestTemplate.getForObject("http://localhost:" + port + "/person?required=Karl&omittable", String.class))
            .isEqualTo("Karl, Omittable[null]");
    }

}
