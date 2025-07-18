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
package com.osmerion.omittable.jackson.internal;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.osmerion.omittable.Omittable;
import org.jspecify.annotations.Nullable;

public final class OmittableSerializer extends ReferenceTypeSerializer<Omittable<?>> {

    public OmittableSerializer(ReferenceType fullType, boolean staticTyping, @Nullable TypeSerializer vts, JsonSerializer<Object> ser) {
        super(fullType, staticTyping, vts, ser);
    }

    private OmittableSerializer(
        OmittableSerializer base, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSer,
        NameTransformer unwrapper, Object suppressableValue, boolean suppressNulls
    ) {
        super(base, property, vts, valueSer, unwrapper, suppressableValue, suppressNulls);
    }

    @Override
    protected ReferenceTypeSerializer<Omittable<?>> withResolved(
        BeanProperty prop, TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper
    ) {
        return new OmittableSerializer(this, prop, vts, valueSer, unwrapper, _suppressableValue, _suppressNulls);
    }

    @Override
    public ReferenceTypeSerializer<Omittable<?>> withContentInclusion(Object suppressableValue, boolean suppressNulls) {
        return new OmittableSerializer(this, _property, _valueTypeSerializer, _valueSerializer, _unwrapper, suppressableValue, suppressNulls);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Omittable<?> value) {
        return value.isAbsent();
    }

    @Override
    protected boolean _isValuePresent(Omittable<?> value) {
        return value.isPresent();
    }

    @Override
    protected Object _getReferenced(Omittable<?> value) {
        return value.orElseThrow();
    }

    @Override
    protected @Nullable Object _getReferencedIfPresent(Omittable<?> value) {
        return value.isPresent() ? value.orElseThrow() : null;
    }

}
