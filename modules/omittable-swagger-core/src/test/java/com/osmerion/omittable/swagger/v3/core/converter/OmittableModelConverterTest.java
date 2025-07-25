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
package com.osmerion.omittable.swagger.v3.core.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osmerion.omittable.Omittable;
import com.osmerion.omittable.jackson.OmittableModule;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public final class OmittableModelConverterTest {

    @Test
    public void testModelConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OmittableModule());

        ModelConverters converters = new ModelConverters();
        converters.addConverter(new OmittableModelConverter(objectMapper));

        assertThat(converters.read(PersonUpdate.class))
            .hasSize(1)
            .extractingByKey("PersonUpdate")
            .satisfies(
                schema -> assertThat(schema)
                    .extracting(Schema::getRequired)
                    .asInstanceOf(LIST)
                    .containsExactly("required", "requiredNullable"),
                schema -> assertThat(schema)
                    .extracting(Schema::getProperties)
                    .asInstanceOf(InstanceOfAssertFactories.map(String.class, Schema.class))
                    .containsOnlyKeys("name", "required", "requiredNullable", "nullable")
                    .hasEntrySatisfying(
                        "name",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "required",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "requiredNullable",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "nullable",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
            );
    }

    record PersonUpdate(
        Omittable<String> name,
        String required,
        @Nullable String requiredNullable,
        Omittable<@Nullable String> nullable
    ) {}

}
