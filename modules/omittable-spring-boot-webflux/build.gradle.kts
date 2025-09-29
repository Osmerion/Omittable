plugins {
    id("com.osmerion.java-base-conventions")
    id("com.osmerion.maven-publish-conventions")
    `java-library`
    `jvm-test-suite`
}

java {
    withSourcesJar()
    withJavadocJar()
}

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter()

            dependencies {
                implementation(project())

                implementation(platform(libs.spring.boot.dependencies))
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
            }
        }
        register<JvmTestSuite>("springDocIntegrationTest") {
            useJUnitJupiter()

            dependencies {
                implementation(project())

                implementation(platform(libs.spring.boot.dependencies))
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.springdoc.openapi.starter.webflux.ui)
            }
        }
    }
}

tasks {
    check {
        dependsOn(testing.suites.named("integrationTest"))
        dependsOn(testing.suites.named("springDocIntegrationTest"))
    }
}

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])

        pom {
            description = "Spring Boot WebFlux support for Omittable types."
        }
    }
}

dependencies {
    api(project(":omittable-spring-webflux"))
    api(project(":omittable-swagger-core"))

    api(platform(libs.spring.boot.dependencies))
    api(libs.spring.boot.autoconfigure)

    compileOnly(libs.springdoc.openapi.starter.common)
}
