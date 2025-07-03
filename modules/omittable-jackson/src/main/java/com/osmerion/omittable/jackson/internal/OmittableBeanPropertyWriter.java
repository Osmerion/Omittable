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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.osmerion.omittable.Omittable;

public final class OmittableBeanPropertyWriter extends BeanPropertyWriter {

    OmittableBeanPropertyWriter(BeanPropertyWriter base) {
        super(base);
    }

    private OmittableBeanPropertyWriter(BeanPropertyWriter base, PropertyName propertyName) {
        super(base);
    }

    @Override
    protected BeanPropertyWriter _new(PropertyName newName) {
        return new OmittableBeanPropertyWriter(this, newName);
    }

    @Override
    public BeanPropertyWriter unwrappingWriter(NameTransformer unwrapper) {
        return new OmittableUnwrappingBeanPropertyWriter(this, unwrapper);
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        if (this._nullSerializer == null) {
            Object value = this.get(bean);
            if (value == null || value.equals(Omittable.absent())) {
                return;
            }
        }

        super.serializeAsField(bean, gen, prov);
    }

}
