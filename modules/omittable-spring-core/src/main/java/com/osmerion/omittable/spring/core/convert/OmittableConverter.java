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
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Set;

/**
 * A {@link GenericConverter} that can convert between different generic types of {@link Omittable} by converting the
 * wrapped value (if necessary).
 *
 * @since   0.5.0
 *
 * @author  Leon Linhart
 */
public final class OmittableConverter implements GenericConverter {

    private final ConversionService conversionService;

    /**
     * Creates a new {@link OmittableConverter}.
     *
     * @param conversionService the conversion service to use for converting the wrapped values
     *
     * @since   0.5.0
     */
    public OmittableConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(Omittable.class, Omittable.class));
    }

    @Override
    public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        /* If the source instance is null, we can just return null. */
        if (source == null) return null;

        /* Nothing to do. Type-erasure got us covered. */
        if (source instanceof Omittable.Absent) return source;

        if (source instanceof Omittable.Present<?> present && sourceType.getResolvableType().hasGenerics() && targetType.getResolvableType().hasGenerics()) {
            Object sourceValue = present.value();

            /* Type-erasure got us covered again. */
            if (sourceValue == null) return targetType.getType().cast(source);

            /* A non-null value actually has to be converted. */
            TypeDescriptor sourceGenericType = TypeDescriptor.valueOf(sourceValue.getClass());
            Object targetValue = this.conversionService.convert(present.value(), sourceGenericType, new GenericTypeDescriptor(targetType));
            return Omittable.of(targetValue);
        }

        return source;
    }

    private static class GenericTypeDescriptor extends TypeDescriptor {

        public GenericTypeDescriptor(TypeDescriptor typeDescriptor) {
            super(typeDescriptor.getResolvableType().getGeneric(), null, typeDescriptor.getAnnotations());
        }

    }

}
