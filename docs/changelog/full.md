### 0.3.0

_Released 2025 Aug 04_

#### Improvements

- The Spring Boot modules originally intended for `0.2.0` are now published.


---

### 0.2.0

_Released 2025 Jul 28_

#### Improvements

- Added more utility functions to more easily retrieve the value described by an
  `Omittable`.
- Added JSpecify dependency and explicit nullability markers.
- Added a Bill of Materials (BoM) to keep Omittable module versions in sync.
    - This is available in the `omittable-bom` artifact.
- Added an `omittable-swagger-core` module to provide Swagger support for
  omittable types via custom `ModelConverter`.
- Added `omittable-spring-webmvc` and `omittable-spring-boot-webmvc` modules
  that provide integration with Spring's servlet API.
- Added `omittable-spring-webflux` and `omittable-spring-boot-webflux` modules
  that provide integration with Spring's servlet API.

#### Fixes

- Replaced placeholder metadata for Jackson module.
- The `omittable` module now declares a dependency on Kotlin's standard library
  to avoid errors during class loading related to compiler-generated checks.

#### Breaking Changes

- Removed `Omittable.getOrThrow` in favor of `Omittable.orElseThrow`.


---

### 0.1.0

_Released 2025 Jul 11_

#### Overview

When developing RESTful APIs, it is often necessary to distinguish between
absence of a value and a null value to properly support partial updates.

Imaging a user profile with an integer ID, a name, and a birthday. Imagine a
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
changed. Not only this is inefficient, it also obscures the intent of the user
which makes it harder for the server to authorize the request. This

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
