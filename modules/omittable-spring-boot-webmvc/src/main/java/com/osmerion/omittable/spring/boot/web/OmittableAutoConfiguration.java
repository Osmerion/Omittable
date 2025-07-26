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
package com.osmerion.omittable.spring.boot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osmerion.omittable.Omittable;
import com.osmerion.omittable.jackson.OmittableModule;
import com.osmerion.omittable.spring.web.OmittableRequestParamMethodArgumentResolver;
import com.osmerion.omittable.swagger.v3.core.converter.OmittableModelConverter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Omittable support.
 *
 * @since   0.2.0
 *
 * @author  Leon Linhart
 */
@AutoConfiguration
@ConditionalOnWebApplication(type =  ConditionalOnWebApplication.Type.SERVLET)
public class OmittableAutoConfiguration {

    @Bean
    public OmittableRequestParamMethodArgumentResolver omittableRequestParamMethodArgumentResolver() {
        return new OmittableRequestParamMethodArgumentResolver();
    }

    @Bean
    public WebMvcConfigurer omittableWebMvcConfigurer(OmittableRequestParamMethodArgumentResolver resolver) {
        return new WebMvcConfigurer() {

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(resolver);
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
        public ParameterCustomizer omittableParameterCustomizer() {
            return (p, m) -> {
                p.setRequired(m.getParameter().getType().equals(Omittable.class));
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
