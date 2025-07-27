# Omittable

[![License](https://img.shields.io/badge/license-Apache%202.0-yellowgreen.svg?style=for-the-badge&label=License)](https://github.com/Osmerion/Omittable/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.osmerion.omittable/omittable.svg?style=for-the-badge&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.osmerion.omittable/omittable)
![Kotlin](https://img.shields.io/badge/Kotlin-2%2E2-green.svg?style=for-the-badge&color=a97bff&logo=Kotlin)
![Java](https://img.shields.io/badge/Java-17-green.svg?style=for-the-badge&color=b07219&logo=Java)

A tiny Kotlin Multiplatform library that provides an Omittable type to be used
in cases where the absence of a value is semantically different from a null
value.


## Why?

When developing RESTful APIs, it is often necessary to distinguish between the
absence of a value and a null value to support partial updates properly.

Consider a user profile with an integer ID, a name, and a birthday. Imagine a
user wants to update their name, but not their birthday. This could be
implemented by updating the entire profile:

```sh
curl -X PUT https://api.example.com/users/123 \
    -H "Content-Type: application/json" \
    -d '{
        "name": "John Doe",
        "birthday": "2005-07-11T10:34:47+00:00"
    }'
```

In this case, the birthday is sent as well, even though it should not be
changed. Not only this is inefficient, but it also obscures the intent of the
user which makes it harder for the server to authorize the request.

A more practical and scalable approach is to use partial updates:

```sh
curl -X PATCH https://api.example.com/users/123 \
    -H "Content-Type: application/json" \
    -d '{
        "name": "John Doe"
    }'
```

Here, the `birthday` field is omitted to indicate that the server should not
change the birthday. Similarly, if the user wants to remove their birthday from
their profile, they could explicitly set it to `null`:

```sh
curl -X PATCH https://api.example.com/users/123 \
    -H "Content-Type: application/json" \
    -d '{
        "birthday": null
    }'
```

While this is a common pattern that makes for a clean API, it is tricky to
translate this into DTOs in most languages since there is typically no
difference between `null` and the absence of a value. The `Omittable` type
solves this by (re-)introducing a semantic distinction between the two.


### Omittable vs. Optional

`Omittable` is similar to Java's `Optional` in that is a container type for
value. Like `Optional`, an `Omittable` should never be set to `null`. However,
while `Optional` is primarily useful as a replacement for `null` that enabled
operator chaining without language modifications, `Omittable` can be used to
wrap values (including `null`s) to create a semantic distinction between absence
of a value and a value that is explicitly set to `null`.


## Usage with Kotlin

A typical DTO written in Kotlin could look like this:

```kotlin
@Serializable
data class UserDto(
    val name: Omittable<String> = Omittable.absent(),
    val birthday: Omittable<Instant> = Omittable.absent()
)
```

Such a DTO can be used with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization):

```kotlin
val dto = TestDto(name = Omittable.of("John Doe"))
val result = json.encodeToString(TestDto.serializer(), dto)

assertEquals(
    """
    {
        "name": "John Doe"
    }
    """.trimIndent(),
    result
)

assertEquals(dto, json.decodeFromString<TestDto>(result))
```


## Usage with Java

While Omittable is written in Kotlin, it does not require Kotlin and is designed
to work smoothly with Java as well.

A typical DTO written in Java could look like this:

```java
public record UserDto(
    Omittable<String> name,
    Omittable<Instant> birthday
) {}
```

Omittable supports Java's pattern matching:

```java
public void process(UserDto user) {
    // Omittable is designed to work seamlessly with Java's pattern matching and
    // can be used with type patterns in instanceof...
    if (user.name() instanceof Omittable.Present(String name)) {
        System.out.println("User's name is: " + name);
    } else {
        System.out.println("User's name is absent");
    }
    
    // ... and switch cases.
    switch (user.birthday()) {
        case Omittable.Present(Instant birthday) -> System.out.println("User's birthday is: " + birthday);
        default -> System.out.println("User's birthday is absent");
    }
}
```


### Jackson

The `omittable-jackson` artifact provides the `OmittableModule` which can be
used to serialize and deserialize `Omittable` values with Jackson.

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new OmittableModule());
mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

UserDto user = mapper.readValue(
    """
    {
        "name": "John Doe"
    }
    """,
    UserDto.class
);

assert user.name().isPresent();
assert user.birthday().isAbsent();
```


## Swagger Core

The `omittable-swagger-core` artifact provides a model converter for omittable
types to generate seamless OpenAPI specifications.

```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new OmittableModule());

ModelConverters converters = new ModelConverters();
converters.addConverter(new OmittableModelConverter(objectMapper));

converters.read(MyModel.class);
```


### Spring

The `omittable-spring-webflux` and `omittable-spring-webmvc` artifacts provide
support for using `Omittable` in Spring WebFlux and Spring WebMVC respectively.
Both artifacts provide a handler method argument resolver for their respective
frameworks that allows using omittable types as reqeust parameters.

The `omittable-spring-boot-webflux` and `omittable-spring-boot-webmvc` modules
provide autoconfiguration on top of the respective artifacts.


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/current/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 24 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the project
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright 2025 Leon Linhart

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
