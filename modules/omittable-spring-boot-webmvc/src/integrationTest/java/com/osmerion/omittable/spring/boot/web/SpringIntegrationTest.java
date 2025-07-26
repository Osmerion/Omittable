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
package com.osmerion.omittable.spring.boot.web;

import com.example.Main;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
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
public final class SpringIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSpringConfiguration() throws JsonProcessingException {
        String response = this.testRestTemplate.getForObject("http://localhost:" + port + "/v3/api-docs", String.class);
        JsonNode node = this.objectMapper.readTree(response);
        String prettyJson = Json.pretty().writeValueAsString(node).replaceAll("\r", "");

        assertThat(prettyJson)
            .isEqualTo(
                """
                {
                  "openapi" : "3.1.0",
                  "servers" : [ {
                    "url" : "http://localhost:%s",
                    "description" : "Generated server url"
                  } ],
                  "paths" : {
                    "/person" : {
                      "get" : {
                        "tags" : [ "person-controller" ],
                        "operationId" : "foo",
                        "parameters" : [ {
                          "name" : "arg0",
                          "in" : "query",
                          "required" : true,
                          "schema" : {
                            "type" : "string"
                          }
                        } ],
                        "responses" : {
                          "200" : {
                            "description" : "OK"
                          }
                        }
                      },
                      "patch" : {
                        "tags" : [ "person-controller" ],
                        "operationId" : "patchPerson",
                        "parameters" : [ {
                          "name" : "arg0",
                          "in" : "query",
                          "required" : false,
                          "schema" : {
                            "$ref" : "#/components/schemas/PersonUpdate"
                          }
                        } ],
                        "responses" : {
                          "200" : {
                            "description" : "OK"
                          }
                        }
                      }
                    }
                  },
                  "components" : {
                    "schemas" : {
                      "PersonUpdate" : {
                        "type" : "object",
                        "properties" : {
                          "name" : {
                            "type" : "string"
                          },
                          "required" : {
                            "type" : "string"
                          },
                          "requiredNullable" : {
                            "type" : "string"
                          },
                          "nullable" : {
                            "type" : "string"
                          }
                        },
                        "required" : [ "required", "requiredNullable" ]
                      }
                    }
                  }
                }""",
                this.port
            );
    }

}
