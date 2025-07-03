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
package com.osmerion.omittable.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osmerion.omittable.Omittable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public final class OmittableJacksonJavaTest {

    static class TestDto {
        private Omittable<String> name = Omittable.absent();
        private Omittable<Integer> count = Omittable.absent();
        private Omittable<String> nullableValue = Omittable.absent();

        // Getters and setters are needed for Jackson in Java
        public Omittable<String> getName() { return name; }
        public void setName(Omittable<String> name) { this.name = name; }
        public Omittable<Integer> getCount() { return count; }
        public void setCount(Omittable<Integer> count) { this.count = count; }
        public Omittable<String> getNullableValue() { return nullableValue; }
        public void setNullableValue(Omittable<String> nullableValue) { this.nullableValue = nullableValue; }
    }

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OmittableModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    @Test
    void shouldSerializePresentValuesCorrectly() throws Exception {
        TestDto dto = new TestDto();
        dto.setName(Omittable.of("Test"));
        dto.setCount(Omittable.of(123));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"name\":\"Test\"");
        assertThat(json).contains("\"count\":123");
        assertThat(json).doesNotContain("nullableValue");
    }

    @Test
    void shouldOmitAbsentValuesFromJsonOutput() throws Exception {
        TestDto dto = new TestDto();
        dto.setName(Omittable.of("Only Name"));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).isEqualTo("{\"name\":\"Only Name\"}");
    }

    @Test
    void shouldSerializeAPresentButNullValue() throws Exception {
        TestDto dto = new TestDto();
        dto.setNullableValue(Omittable.of(null));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).isEqualTo("{\"nullableValue\":null}");
    }

    @Test
    void shouldDeserializePresentValuesCorrectly() throws Exception {
        String json = "{\"name\":\"Test\",\"count\":123,\"nullableValue\":null}";
        TestDto dto = objectMapper.readValue(json, TestDto.class);

        assertThat(dto.getName()).isEqualTo(Omittable.of("Test"));
        assertThat(dto.getCount()).isEqualTo(Omittable.of(123));
        assertThat(dto.getNullableValue()).isEqualTo(Omittable.of(null));
    }

    @Test
    void shouldDeserializeMissingFieldsToAbsent() throws Exception {
        String json = "{\"name\":\"Only Name\"}";
        TestDto dto = objectMapper.readValue(json, TestDto.class);

        assertThat(dto.getName()).isEqualTo(Omittable.of("Only Name"));
        assertThat(dto.getCount()).isEqualTo(Omittable.absent());
        assertThat(dto.getNullableValue()).isEqualTo(Omittable.absent());
    }

}
