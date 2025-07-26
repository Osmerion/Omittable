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
package com.example.web;

import com.example.model.PersonUpdate;
import com.osmerion.omittable.Omittable;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/person")
public final class PersonController {

    @GetMapping
    public Mono<Void> foo(
        @RequestParam(name = "required") String required,
        @RequestParam(name = "omittable") @Nullable Omittable<String> omittable,
        @RequestParam(name = "mono") Mono<Omittable<@Nullable String>> mono
    ) {
        return Mono.empty();
    }

    @PatchMapping
    public Mono<Void> patchPerson(Mono<PersonUpdate> person) {
        return Mono.empty();
    }

}
