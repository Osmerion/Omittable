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
package com.osmerion.omittable.spring.boot.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osmerion.omittable.Omittable;
import com.osmerion.omittable.jackson.OmittableModule;
import com.osmerion.omittable.spring.core.convert.OmittableConverter;
import com.osmerion.omittable.spring.webflux.OmittableRequestParamMethodArgumentResolver;
import com.osmerion.omittable.swagger.v3.core.converter.OmittableModelConverter;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Field;
import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Omittable support.
 *
 * @since   0.2.0
 *
 * @author  Leon Linhart
 */
@AutoConfiguration
@ConditionalOnWebApplication(type =  ConditionalOnWebApplication.Type.REACTIVE)
public class OmittableReactiveAutoConfiguration {

    public OmittableReactiveAutoConfiguration() {}

    @Bean
    public OmittableRequestParamMethodArgumentResolver omittableRequestParamMethodArgumentResolver(
        @Nullable ConfigurableBeanFactory factory,
        @Lazy ReactiveAdapterRegistry registry
    ) {
        return new OmittableRequestParamMethodArgumentResolver(factory, registry);
    }

    @Bean
    public WebFluxConfigurer omittableWebFluxConfigurer(
        @Lazy ConversionService conversionService
    ) {
        return new WebFluxConfigurer() {

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new OmittableConverter(conversionService));
            }

        };
    }

    @Bean
    public InitializingBean omittableInitializationBean(
        RequestMappingHandlerAdapter requestMappingHandlerAdapter,
        OmittableRequestParamMethodArgumentResolver omittableRequestParamMethodArgumentResolver
    ) {
        return () -> {
            // This is a hack to work around the fact that Spring does not provide a way to customize the order
            Class<RequestMappingHandlerAdapter> requestMappingHandlerAdapterClass = RequestMappingHandlerAdapter.class;
            Field methodResolverField = requestMappingHandlerAdapterClass.getDeclaredField("methodResolver");

            methodResolverField.setAccessible(true);

            try {
                Object methodResolver = methodResolverField.get(requestMappingHandlerAdapter);
                Class<?> controllerMethodResolverClass = methodResolver.getClass();
                Field requestMappingResolversField = controllerMethodResolverClass.getDeclaredField("requestMappingResolvers");

                requestMappingResolversField.setAccessible(true);

                try {
                    @SuppressWarnings("unchecked")
                    List<HandlerMethodArgumentResolver> handlerMethodArgumentResolvers = (List<HandlerMethodArgumentResolver>) requestMappingResolversField.get(methodResolver);
                    handlerMethodArgumentResolvers.add(0, omittableRequestParamMethodArgumentResolver);
                } finally {
                    requestMappingResolversField.setAccessible(false);
                }
            } finally {
                methodResolverField.setAccessible(false);
            }
        };
    }

    @Configuration
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public static class OmittableJacksonAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean // Allow users to override this by defining their own bean of this type
        public OmittableModule omittableJacksonModule() {
            return new OmittableModule();
        }

    }

    @Configuration
    @ConditionalOnClass(name = "io.swagger.v3.core.converter.ModelConverter")
    public static class OmittableSpringdocAutoConfiguration {

        @Bean
        public ParameterCustomizer omittableParameterCustomizer(ReactiveAdapterRegistry registry) {
            return (p, m) -> {
                Class<?> type = m.getNestedParameterType();
                ReactiveAdapter adapter = registry.getAdapter(type);

                if (adapter != null) {
                    m = m.nested();
                    type = m.getNestedParameterType();
                }

                if (type.equals(Omittable.class)) {
                    p.setRequired(false); // Omittable parameters are never required
                }

                return p;
            };
        }

        @Bean
        @ConditionalOnMissingBean
        public OmittableModelConverter omittableModelConverter(ObjectMapper objectMapper) {
            return new OmittableModelConverter(objectMapper);
        }

    }

}
