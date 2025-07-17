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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.osmerion.omittable.Omittable;
import com.osmerion.omittable.jackson.internal.*;
import org.jspecify.annotations.Nullable;

public final class OmittableModule extends Module {

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new OmittableSerializers());
        context.addDeserializers(new OmittableDeserializers());

        // And to fully support Omittables, need to modify type info:
        context.addTypeModifier(new OmittableTypeModifier());

        // Allow enabling "treat Optional.empty() like Java nulls"
        context.addBeanSerializerModifier(new OmittableBeanSerializerModifier());
    }

    @Override
    public String getModuleName() {
        return "OmittableModule";
    }

    @Override
    public Version version() {
        return new Version(
            BuildConfig.VERSION_MAJOR,
            BuildConfig.VERSION_MINOR,
            BuildConfig.VERSION_PATCH,
            BuildConfig.SNAPSHOT_INFO,
            BuildConfig.GROUP_ID,
            BuildConfig.ARTIFACT_ID
        );
    }

    private static final class OmittableDeserializers extends Deserializers.Base {

        @Override
        public @Nullable JsonDeserializer<?> findReferenceDeserializer(
            ReferenceType refType,
            DeserializationConfig config,
            BeanDescription beanDesc,
            TypeDeserializer contentTypeDeserializer,
            JsonDeserializer<?> contentDeserializer
        ) {
            if (refType.hasRawClass(Omittable.class)) {
                return new OmittableDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
            }

            return null;
        }

    }

    private static final class OmittableSerializers extends Serializers.Base {

        @Override
        public @Nullable JsonSerializer<?> findReferenceSerializer(
            SerializationConfig config,
            ReferenceType refType,
            BeanDescription beanDesc,
            @Nullable TypeSerializer contentTypeSerializer,
            JsonSerializer<Object> contentValueSerializer
        ) {
            Class<?> raw = refType.getRawClass();
            if (Omittable.class.isAssignableFrom(raw)) {
                boolean staticTyping = (contentTypeSerializer == null) && config.isEnabled(MapperFeature.USE_STATIC_TYPING);
                return new OmittableSerializer(refType, staticTyping, contentTypeSerializer, contentValueSerializer);
            }

            return null;
        }

    }

}
