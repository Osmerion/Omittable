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
package com.osmerion.omittable.spring.web;

import com.osmerion.omittable.Omittable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/**
 * A {@link HandlerMethodArgumentResolver} that resolves {@link Omittable} {@link RequestParam request parameters}.
 *
 * @since   0.2.0
 *
 * @author  Leon Linhart
 */
public final class OmittableRequestParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    /**
     * {@inheritDoc}
     *
     * @since   0.2.0
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Omittable.class)
            && parameter.hasParameterAnnotation(RequestParam.class);
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.2.0
     */
    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
        return (ann != null ? new RequestParamNamedValueInfo(ann) : new RequestParamNamedValueInfo());
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.2.0
     */
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) {
        String[] paramValues;

        if (request.getParameterMap().containsKey(name)) {
            paramValues = request.getParameterValues(name);
        } else if (request.getParameterMap().containsKey(name + "[]")) {
            paramValues = request.getParameterValues(name + "[]");
        } else {
            return Omittable.absent();
        }

        assert paramValues != null;
        return Omittable.of(paramValues.length == 1 ? paramValues[0] : paramValues);
    }

    private static final class RequestParamNamedValueInfo extends NamedValueInfo {

        public RequestParamNamedValueInfo() {
            super("", false, ValueConstants.DEFAULT_NONE);
        }

        public RequestParamNamedValueInfo(RequestParam annotation) {
            super(annotation.name(), annotation.required(), annotation.defaultValue());
        }

    }

}
