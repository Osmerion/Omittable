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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.osmerion.omittable.Omittable;

import java.util.List;

public final class OmittableBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(
        SerializationConfig config,
        BeanDescription beanDesc,
        List<BeanPropertyWriter> beanProperties
    ) {
        for (int i = 0; i < beanProperties.size(); ++i) {
            BeanPropertyWriter writer = beanProperties.get(i);
            JavaType type = writer.getType();

            if (type.isTypeOrSubTypeOf(Omittable.class)) {
                beanProperties.set(i, new OmittableBeanPropertyWriter(writer));
            }
        }

        return beanProperties;
    }

}
