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
package com.osmerion.omittable.swagger.v3.core.converter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.osmerion.omittable.Omittable;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

import static io.swagger.v3.core.util.RefUtils.constructRef;

/**
 * A {@link ModelConverter} that handles the {@link Omittable} type.
 *
 * @since   0.2.0
 *
 * @author  Leon Linhart
 */
public final class OmittableModelConverter extends ModelResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(OmittableModelConverter.class);

    private static final String SCHEMA_COMPONENT_PREFIX = "#/components/schemas/";
    private static final int SCHEMA_COMPONENT_PREFIX_LENGTH = SCHEMA_COMPONENT_PREFIX.length();

    private final boolean openApi31;
    private final Schema.SchemaResolution schemaResolution;

    public OmittableModelConverter(ObjectMapper mapper) {
        this(mapper, false);
    }

    public OmittableModelConverter(ObjectMapper mapper, boolean openApi31) {
        this(mapper, openApi31, Schema.SchemaResolution.DEFAULT);
    }

    public OmittableModelConverter(ObjectMapper mapper, boolean openApi31, Schema.SchemaResolution schemaResolution) {
        super(mapper);
        this.openApi31 = openApi31;
        this.schemaResolution = schemaResolution;
    }

    @Override
    public @Nullable Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        JavaType type = this.objectMapper().constructType(annotatedType.getType());

        if (!type.hasRawClass(Omittable.class)) {
            Schema<?> model = (chain.hasNext()) ? chain.next().resolve(annotatedType, context, chain) : null;
            if (model == null) return null;

            if (model.get$ref() != null && model.get$ref().startsWith(SCHEMA_COMPONENT_PREFIX)) {
                model = context.getDefinedModels().get(model.get$ref().substring(SCHEMA_COMPONENT_PREFIX_LENGTH));
            }

            List<String> requiredProps = new ArrayList<>();

            final BeanDescription beanDesc;
            {
                BeanDescription recurBeanDesc = _mapper.getSerializationConfig().introspect(type);

                HashSet<String> visited = new HashSet<>();
                JsonSerialize jsonSerialize = recurBeanDesc.getClassAnnotations().get(JsonSerialize.class);
                while (jsonSerialize != null && !Void.class.equals(jsonSerialize.as())) {
                    String asName = jsonSerialize.as().getName();
                    if (visited.contains(asName)) break;
                    visited.add(asName);

                    recurBeanDesc = _mapper.getSerializationConfig().introspect(
                        _mapper.constructType(jsonSerialize.as())
                    );
                    jsonSerialize = recurBeanDesc.getClassAnnotations().get(JsonSerialize.class);
                }
                beanDesc = recurBeanDesc;
            }

            final XmlAccessorType xmlAccessorTypeAnnotation = beanDesc.getClassAnnotations().get(XmlAccessorType.class);

            List<Schema> props = new ArrayList<>();

            // see if @JsonIgnoreProperties exist
            Set<String> propertiesToIgnore = resolveIgnoredProperties(beanDesc.getClassAnnotations(), annotatedType.getCtxAnnotations());

            List<BeanPropertyDefinition> properties = beanDesc.findProperties();
            List<String> ignoredProps = getIgnoredProperties(beanDesc);
            properties.removeIf(p -> ignoredProps.contains(p.getName()));
            for (BeanPropertyDefinition propDef : properties) {
                Schema<?> property;
                String propName = propDef.getName();
                Annotation[] annotations;

                AnnotatedMember member = propDef.getPrimaryMember();
                if (member == null) {
                    final BeanDescription deserBeanDesc = _mapper.getDeserializationConfig().introspect(type);
                    List<BeanPropertyDefinition> deserProperties = deserBeanDesc.findProperties();
                    for (BeanPropertyDefinition prop : deserProperties) {
                        if (StringUtils.isNotBlank(prop.getInternalName()) && prop.getInternalName().equals(propDef.getInternalName())) {
                            member = prop.getPrimaryMember();
                            break;
                        }
                    }
                }

                // hack to avoid clobbering properties with get/is names
                // it's ugly but gets around https://github.com/swagger-api/swagger-core/issues/415
                if (propDef.getPrimaryMember() != null) {
                    final JsonProperty jsonPropertyAnn = propDef.getPrimaryMember().getAnnotation(JsonProperty.class);
                    if (jsonPropertyAnn == null || !jsonPropertyAnn.value().equals(propName)) {
                        if (member != null) {
                            java.lang.reflect.Member innerMember = member.getMember();
                            if (innerMember != null) {
                                String altName = innerMember.getName();
                                if (altName != null) {
                                    final int length = altName.length();
                                    for (String prefix : Arrays.asList("get", "is")) {
                                        final int offset = prefix.length();
                                        if (altName.startsWith(prefix) && length > offset
                                            && !Character.isUpperCase(altName.charAt(offset))) {
                                            propName = altName;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                PropertyMetadata md = propDef.getMetadata();

                if (member != null && !ignore(member, xmlAccessorTypeAnnotation, propName, propertiesToIgnore, propDef)) {
                    List<Annotation> annotationList = new ArrayList<>();
                    for (Annotation a : member.annotations()) {
                        annotationList.add(a);
                    }

                    annotations = annotationList.toArray(new Annotation[0]);

                    if (hiddenByJsonView(annotations, annotatedType)) {
                        continue;
                    }

                    JavaType propType = member.getType();
                    if (propType != null && "void".equals(propType.getRawClass().getName())) {
                        if (member instanceof AnnotatedMethod) {
                            propType = ((AnnotatedMethod) member).getParameterType(0);
                        }
                    }

                    String propSchemaName = null;
                    io.swagger.v3.oas.annotations.media.Schema ctxSchema = AnnotationsUtils.getSchemaAnnotation(annotations);
                    if (AnnotationsUtils.hasSchemaAnnotation(ctxSchema)) {
                        if (!StringUtils.isBlank(ctxSchema.name())) {
                            propSchemaName = ctxSchema.name();
                        }
                    }
                    io.swagger.v3.oas.annotations.media.ArraySchema ctxArraySchema = AnnotationsUtils.getArraySchemaAnnotation(annotations);
                    if (propSchemaName == null) {
                        if (AnnotationsUtils.hasArrayAnnotation(ctxArraySchema)) {
                            if (AnnotationsUtils.hasSchemaAnnotation(ctxArraySchema.schema())) {
                                if (!StringUtils.isBlank(ctxArraySchema.schema().name())) {
                                    propSchemaName = ctxArraySchema.schema().name();
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(propSchemaName)) {
                        propName = propSchemaName;
                    }
                    Annotation propSchemaOrArray = AnnotationsUtils.mergeSchemaAnnotations(annotations, propType);
                    final io.swagger.v3.oas.annotations.media.Schema propResolvedSchemaAnnotation =
                        propSchemaOrArray == null ?
                            null :
                            propSchemaOrArray instanceof io.swagger.v3.oas.annotations.media.ArraySchema ?
                                ((io.swagger.v3.oas.annotations.media.ArraySchema) propSchemaOrArray).schema() :
                                (io.swagger.v3.oas.annotations.media.Schema) propSchemaOrArray;

                    io.swagger.v3.oas.annotations.media.Schema.AccessMode accessMode = resolveAccessMode(propDef, type, propResolvedSchemaAnnotation);

                    io.swagger.v3.oas.annotations.media.Schema.RequiredMode requiredMode;

                    if (propType.getRawClass().equals(Omittable.class)) {
                        requiredMode = resolveRequiredMode(propResolvedSchemaAnnotation);
                    } else {
                        requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
                    }

                    Annotation[] ctxAnnotation31 = null;
                    Schema.SchemaResolution resolvedSchemaResolution = AnnotationsUtils.resolveSchemaResolution(this.schemaResolution, ctxSchema);
                    if (
                        Schema.SchemaResolution.ALL_OF.equals(resolvedSchemaResolution) ||
                            Schema.SchemaResolution.ALL_OF_REF.equals(resolvedSchemaResolution) ||
                            openApi31) {
                        List<Annotation> ctxAnnotations31List = new ArrayList<>();
                        if (annotations != null) {
                            for (Annotation a : annotations) {
                                if (
                                    !(a instanceof io.swagger.v3.oas.annotations.media.Schema) &&
                                        !(a instanceof io.swagger.v3.oas.annotations.media.ArraySchema)) {
                                    ctxAnnotations31List.add(a);
                                }
                                if ((ctxSchema != null) && (!ctxSchema.implementation().equals(Void.class) || StringUtils.isNotEmpty(ctxSchema.type()))) {
                                    ctxAnnotations31List.add(a);
                                }
                            }
                            ctxAnnotation31 = ctxAnnotations31List.toArray(new Annotation[0]);
                        }
                    }
                    Set<Annotation> validationInvocationAnnotations = null;
                    if (validatorProcessor != null) {
                        validationInvocationAnnotations = validatorProcessor.resolveInvocationAnnotations(annotations);
                        if (validationInvocationAnnotations == null) {
                            validationInvocationAnnotations = validatorProcessor.resolveInvocationAnnotations(annotatedType.getCtxAnnotations());
                        } else {
                            validationInvocationAnnotations.addAll(validatorProcessor.resolveInvocationAnnotations(annotatedType.getCtxAnnotations()));
                        }
                    }
                    if (validationInvocationAnnotations == null) {
                        validationInvocationAnnotations = resolveValidationInvocationAnnotations(annotations);
                        validationInvocationAnnotations.addAll(resolveValidationInvocationAnnotations(annotatedType.getCtxAnnotations()));
                    }
                    annotations = Stream.concat(Arrays.stream(annotations), Arrays.stream(validationInvocationAnnotations.toArray(new Annotation[0]))).toArray(Annotation[]::new);
                    if (ctxAnnotation31 != null) {
                        ctxAnnotation31 = Stream.concat(Arrays.stream(ctxAnnotation31), Arrays.stream(validationInvocationAnnotations.toArray(new Annotation[0]))).toArray(Annotation[]::new);
                    }
                    AnnotatedType aType = new AnnotatedType()
                        .type(propType)
                        .parent(model)
                        .resolveAsRef(annotatedType.isResolveAsRef())
                        .jsonViewAnnotation(annotatedType.getJsonViewAnnotation())
                        .skipSchemaName(true)
                        .schemaProperty(true)
                        .components(annotatedType.getComponents())
                        .propertyName(propName);
                    if (
                        Schema.SchemaResolution.ALL_OF.equals(resolvedSchemaResolution) ||
                            Schema.SchemaResolution.ALL_OF_REF.equals(resolvedSchemaResolution) ||
                            openApi31) {
                        aType.ctxAnnotations(ctxAnnotation31);
                    } else {
                        aType.ctxAnnotations(annotations);
                    }
                    final AnnotatedMember propMember = member;
                    aType.jsonUnwrappedHandler(t -> {
                        JsonUnwrapped uw = propMember.getAnnotation(JsonUnwrapped.class);
                        if (uw != null && uw.enabled()) {
                            t
                                .ctxAnnotations(null)
                                .jsonUnwrappedHandler(null)
                                .resolveAsRef(false);
                            Schema innerModel = context.resolve(t);
                            if (StringUtils.isNotBlank(innerModel.get$ref())) {
                                innerModel = context.getDefinedModels().get(innerModel.get$ref().substring(SCHEMA_COMPONENT_PREFIX_LENGTH));
                            }
                            handleUnwrapped(props, innerModel, uw.prefix(), uw.suffix(), requiredProps);
                            return null;
                        } else {
                            return new Schema<>();
                        }
                    });
                    property = context.resolve(aType);
                    property = clone(property);
                    Schema<?> ctxProperty = null;
                    if (!applySchemaResolution()) {
                        Optional<Schema> reResolvedProperty = AnnotationsUtils.getSchemaFromAnnotation(ctxSchema, annotatedType.getComponents(), null, openApi31, property, schemaResolution, context);
                        if (reResolvedProperty.isPresent()) {
                            property = reResolvedProperty.get();
                        }
                        reResolvedProperty = AnnotationsUtils.getArraySchema(ctxArraySchema, annotatedType.getComponents(), null, openApi31, property, true);
                        if (reResolvedProperty.isPresent()) {
                            property = reResolvedProperty.get();
                        }

                    } else if (Schema.SchemaResolution.ALL_OF.equals(resolvedSchemaResolution) || Schema.SchemaResolution.ALL_OF_REF.equals(resolvedSchemaResolution)) {
                        Optional<Schema> reResolvedProperty = AnnotationsUtils.getSchemaFromAnnotation(ctxSchema, annotatedType.getComponents(), null, openApi31, null, schemaResolution, context);
                        if (reResolvedProperty.isPresent()) {
                            ctxProperty = reResolvedProperty.get();
                        }
                        reResolvedProperty = AnnotationsUtils.getArraySchema(ctxArraySchema, annotatedType.getComponents(), null, openApi31, ctxProperty);
                        if (reResolvedProperty.isPresent()) {
                            ctxProperty = reResolvedProperty.get();
                        }
                    }

                    if (property != null) {
                        Boolean required = md.getRequired();
                        if (!io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED.equals(requiredMode)) {
                            if (required != null && !Boolean.FALSE.equals(required)) {
                                addRequiredItem(model, propName);
                            } else {
                                if (propDef.isRequired()) {
                                    addRequiredItem(model, propName);
                                }
                            }
                        }

                        if (property.get$ref() == null || openApi31) {
                            if (accessMode != null) {
                                switch (accessMode) {
                                    case AUTO:
                                        break;
                                    case READ_ONLY:
                                        property.readOnly(true);
                                        break;
                                    case READ_WRITE:
                                        break;
                                    case WRITE_ONLY:
                                        property.writeOnly(true);
                                        break;
                                    default:
                                }
                            }
                        }
                        final BeanDescription propBeanDesc = _mapper.getSerializationConfig().introspect(propType);
                        if (property != null && !propType.isContainerType()) {
                            if (isObjectSchema(property)) {
                                // create a reference for the property
                                String pName = _typeName(propType, propBeanDesc);
                                if (StringUtils.isNotBlank(property.getName())) {
                                    pName = property.getName();
                                }

                                if (context.getDefinedModels().containsKey(pName)) {
                                    if (Schema.SchemaResolution.INLINE.equals(resolvedSchemaResolution)) {
                                        property = context.getDefinedModels().get(pName);
                                    } else if (Schema.SchemaResolution.ALL_OF.equals(resolvedSchemaResolution) && ctxProperty != null) {
                                        property = new Schema<>()
                                            .addAllOfItem(ctxProperty)
                                            .addAllOfItem(new Schema<>().$ref(constructRef(pName)));
                                    } else if (Schema.SchemaResolution.ALL_OF_REF.equals(resolvedSchemaResolution) && ctxProperty != null) {
                                        property = ctxProperty.addAllOfItem(new Schema<>().$ref(constructRef(pName)));
                                    } else {
                                        property = new Schema<>().$ref(constructRef(pName));
                                    }
                                    property = clone(property);
                                    // TODO: why is this needed? is it not handled before?
                                    if (openApi31 || Schema.SchemaResolution.INLINE.equals(resolvedSchemaResolution)) {
                                        Optional<Schema> reResolvedProperty = AnnotationsUtils.getSchemaFromAnnotation(ctxSchema, annotatedType.getComponents(), null, openApi31, property, this.schemaResolution, context);
                                        if (reResolvedProperty.isPresent()) {
                                            property = reResolvedProperty.get();
                                        }
                                        reResolvedProperty = AnnotationsUtils.getArraySchema(ctxArraySchema, annotatedType.getComponents(), null, openApi31, property);
                                        if (reResolvedProperty.isPresent()) {
                                            property = reResolvedProperty.get();
                                        }
                                    }
                                }
                            } else if (property.get$ref() != null) {
                                if (applySchemaResolution()) {
                                    if (Schema.SchemaResolution.ALL_OF.equals(resolvedSchemaResolution) && ctxProperty != null) {
                                        property = new Schema<>()
                                            .addAllOfItem(ctxProperty)
                                            .addAllOfItem(new Schema<>().$ref(StringUtils.isNotEmpty(property.get$ref()) ? property.get$ref() : property.getName()));
                                    } else if (Schema.SchemaResolution.ALL_OF_REF.equals(resolvedSchemaResolution) && ctxProperty != null) {
                                        property = ctxProperty
                                            .addAllOfItem(new Schema<>().$ref(StringUtils.isNotEmpty(property.get$ref()) ? property.get$ref() : property.getName()));
                                    } else {
                                        property = new Schema<>().$ref(StringUtils.isNotEmpty(property.get$ref()) ? property.get$ref() : property.getName());
                                    }
                                } else {
                                    if (StringUtils.isEmpty(property.get$ref())) {
                                        property.$ref(property.getName());
                                    }
                                }
                            }
                        }
                        property.setName(propName);
                        JAXBAnnotationsHelper.apply(propBeanDesc.getClassInfo(), annotations, property);
                        if (property != null && io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED.equals(requiredMode)) {
                            addRequiredItem(model, property.getName());
                        }
                    }
                }
            }

            for (String propertyName : requiredProps) {
                addRequiredItem(model, propertyName);
            }

            return model;
        }

        JavaType innerType = type.containedType(0);
        if (innerType == null) {
            innerType = Json.mapper().constructType(Object.class);
        }

        AnnotatedType innerAnnotatedType = new AnnotatedType(innerType.getRawClass())
            .jsonUnwrappedHandler(annotatedType.getJsonUnwrappedHandler())
            .ctxAnnotations(annotatedType.getCtxAnnotations())
            .parent(annotatedType.getParent())
            .schemaProperty(annotatedType.isSchemaProperty())
            .name(annotatedType.getName())
            .propertyName(annotatedType.getPropertyName())
            .resolveAsRef(annotatedType.isResolveAsRef());

        return (chain.hasNext())
            ? chain.next().resolve(innerAnnotatedType, context, chain)
            : context.resolve(innerAnnotatedType);
    }

    private Schema<?> clone(Schema<?> property) {
        return AnnotationsUtils.clone(property, openApi31);
    }

    private List<String> getIgnoredProperties(BeanDescription beanDescription) {
        AnnotationIntrospector introspector = _mapper.getSerializationConfig().getAnnotationIntrospector();
        JsonIgnoreProperties.Value v = introspector.findPropertyIgnorals(beanDescription.getClassInfo());
        Set<String> ignored = null;
        if (v != null) {
            ignored = v.findIgnoredForSerialization();
        }
        return ignored == null ? Collections.emptyList() : new ArrayList<>(ignored);
    }


    private void handleUnwrapped(List<Schema> props, Schema<?> innerModel, @Nullable String prefix, @Nullable String suffix, List<String> requiredProps) {
        if (StringUtils.isBlank(suffix) && StringUtils.isBlank(prefix)) {
            if (innerModel.getProperties() != null) {
                props.addAll(innerModel.getProperties().values());
                if (innerModel.getRequired() != null) {
                    requiredProps.addAll(innerModel.getRequired());
                }
            }
        } else {
            if (prefix == null) {
                prefix = "";
            }
            if (suffix == null) {
                suffix = "";
            }
            if (innerModel.getProperties() != null) {
                for (Schema<?> prop : innerModel.getProperties().values()) {
                    try {
                        Schema<?> clonedProp = Json.mapper().readValue(Json.pretty(prop), Schema.class);
                        clonedProp.setName(prefix + prop.getName() + suffix);
                        props.add(clonedProp);
                    } catch (IOException e) {
                        LOGGER.error("Exception cloning property", e);
                        return;
                    }
                }
            }
        }
    }

}
