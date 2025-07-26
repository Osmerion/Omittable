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
package com.osmerion.omittable.spring.webflux;

import com.osmerion.omittable.Omittable;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.AbstractNamedValueSyncArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;

/**
 * A {@link HandlerMethodArgumentResolver} that resolves {@link Omittable} {@link RequestParam request parameters}.
 *
 * @since   0.2.0
 *
 * @author  Leon Linhart
 */
public final class OmittableRequestParamMethodArgumentResolver extends AbstractNamedValueSyncArgumentResolver {

    /**
     * Create a new {@link OmittableRequestParamMethodArgumentResolver} instance.
     *
     * @param factory  a bean factory to use for resolving {@code ${...}} placeholder
     *                 and {@code #{...}} SpEL expressions in default values, or {@code null} if default
     *                 values are not expected to contain expressions
     * @param registry for checking reactive type wrappers
     *
     * @since   0.2.0
     */
    public OmittableRequestParamMethodArgumentResolver(
        @Nullable ConfigurableBeanFactory factory,
        ReactiveAdapterRegistry registry
    ) {
        super(factory, registry);
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.2.0
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return checkParameterTypeNoReactiveWrapper(parameter, it -> it.equals(Omittable.class))
            && checkAnnotatedParamNoReactiveWrapper(parameter, RequestParam.class, this::singleParam);
    }

    private boolean singleParam(RequestParam requestParam, Class<?> type) {
        return !Map.class.isAssignableFrom(type) || StringUtils.hasText(requestParam.name());
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
    protected Object resolveNamedValue(String name, MethodParameter param, ServerWebExchange exchange) {
        List<String> paramValues;

        if (exchange.getRequest().getQueryParams().containsKey(name)) {
            paramValues = exchange.getRequest().getQueryParams().get(name);
        } else if (exchange.getRequest().getQueryParams().containsKey(name + "[]")) {
            paramValues = exchange.getRequest().getQueryParams().get(name + "[]");
        } else {
            return Omittable.absent();
        }

        assert paramValues != null;
        return Omittable.of(paramValues.size() == 1 ? paramValues.get(0) : paramValues);
    }

    private static final class RequestParamNamedValueInfo extends NamedValueInfo {

        public RequestParamNamedValueInfo() {
            super("", false, ValueConstants.DEFAULT_NONE);
        }

        public RequestParamNamedValueInfo(RequestParam annotation) {
            super(annotation.name(), false, annotation.defaultValue());
        }

    }

}
