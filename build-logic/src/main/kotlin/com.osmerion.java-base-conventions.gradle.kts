plugins {
    `java-base`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 17
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
