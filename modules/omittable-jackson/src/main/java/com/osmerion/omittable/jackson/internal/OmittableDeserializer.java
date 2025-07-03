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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.osmerion.omittable.Omittable;

public final class OmittableDeserializer extends ReferenceTypeDeserializer<Omittable<?>> {

    public OmittableDeserializer(
        JavaType fullType,
        ValueInstantiator inst,
        TypeDeserializer typeDeser,
        JsonDeserializer<?> deser
    ) {
        super(fullType, inst, typeDeser, deser);
    }

    @Override
    public OmittableDeserializer withResolved(TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser) {
        return new OmittableDeserializer(_fullType, _valueInstantiator, typeDeser, valueDeser);
    }

    @Override
    public Omittable<?> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return Omittable.of(_valueDeserializer.getNullValue(ctxt));
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return getNullValue(ctxt);
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) throws JsonMappingException {
        return getNullValue(ctxt);
    }

    @Override
    public Omittable<?> referenceValue(Object contents) {
        return Omittable.of(contents);
    }

    @Override
    public Object getReferenced(Omittable<?> reference) {
        return reference.isPresent() ? reference.getOrThrow() : null;
    }

    @Override
    public Omittable<?> updateReference(Omittable<?> reference, Object contents) {
        return Omittable.of(contents);
    }

    // Default ought to be fine:
//    public Boolean supportsUpdate(DeserializationConfig config) { }

}
