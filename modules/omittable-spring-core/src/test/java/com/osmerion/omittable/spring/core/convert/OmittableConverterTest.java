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
package com.osmerion.omittable.spring.core.convert;

import com.osmerion.omittable.Omittable;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OmittableConverter}.
 *
 * @author  Leon Linhart
 */
public final class OmittableConverterTest {

    private static final TypeDescriptor OMITTABLE_STRING = new TypeDescriptor(
        ResolvableType.forClassWithGenerics(Omittable.class, String.class),
        Omittable.class,
        new Annotation[0]
    );

    private static final TypeDescriptor OMITTABLE_UUID = new TypeDescriptor(
        ResolvableType.forClassWithGenerics(Omittable.class, UUID.class),
        Omittable.class,
        new Annotation[0]
    );

    @Test
    public void testConvert_Null() {
        ConversionService conversionService = mock(ConversionService.class);
        OmittableConverter converter = new OmittableConverter(conversionService);

        assertThat(converter.convert(null, OMITTABLE_STRING, OMITTABLE_UUID))
            .isNull();
    }

    @Test
    public void testConvert_Absent() {
        ConversionService conversionService = mock(ConversionService.class);
        OmittableConverter converter = new OmittableConverter(conversionService);

        assertThat(converter.convert(Omittable.absent(), OMITTABLE_STRING, OMITTABLE_UUID))
            .isEqualTo(Omittable.absent());
    }

    @Test
    public void testConvert_UUID() {
        ConversionService conversionService = mock(ConversionService.class);
        OmittableConverter converter = new OmittableConverter(conversionService);

        when(conversionService.convert(eq("c92ab1eb-b2bf-408b-b12c-b21fc1f55ddc"), any(), any()))
            .thenReturn(UUID.fromString("c92ab1eb-b2bf-408b-b12c-b21fc1f55ddc"));

        assertThat(converter.convert(Omittable.of("c92ab1eb-b2bf-408b-b12c-b21fc1f55ddc"), OMITTABLE_STRING, OMITTABLE_UUID))
            .isEqualTo(Omittable.of(UUID.fromString("c92ab1eb-b2bf-408b-b12c-b21fc1f55ddc")));
    }

}
