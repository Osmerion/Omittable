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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import com.osmerion.omittable.Omittable;

import java.lang.reflect.Type;

public final class OmittableTypeModifier extends TypeModifier {

    @Override
    public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
        if (type.isReferenceType() || type.isContainerType()) {
            return type;
        }

        Class<?> raw = type.getRawClass();
        JavaType refType;

        if (raw == Omittable.class) {
            refType = type.containedTypeOrUnknown(0);
        } else {
            return type;
        }

        return ReferenceType.upgradeFrom(type, refType);
    }

}
