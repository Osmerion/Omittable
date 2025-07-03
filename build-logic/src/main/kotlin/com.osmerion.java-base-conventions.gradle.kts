plugins {
    `java-base`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 11
    }

    withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
