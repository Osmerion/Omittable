plugins {
    id("com.osmerion.java-base-conventions")
    id("com.osmerion.maven-publish-conventions")
    id("com.osmerion.optional-dependencies")
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
                implementation(libs.spring.boot.starter.web)
                implementation(libs.springdoc.openapi.starter.webmvc.ui)
            }
        }
    }
}

tasks {
    check {
        dependsOn(testing.suites.named("integrationTest"))
    }
}

dependencies {
    api(project(":omittable-spring-webmvc"))
    api(project(":omittable-swagger-core"))

    api(platform(libs.spring.boot.dependencies))
    api(libs.spring.boot.autoconfigure)

    optional(libs.springdoc.openapi.starter.common)
}
