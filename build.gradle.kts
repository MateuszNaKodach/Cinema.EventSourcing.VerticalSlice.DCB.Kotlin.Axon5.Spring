plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.3.10"
}

group = "com.dddheroes"
version = "0.0.1-SNAPSHOT"
description = "cinema"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

extra["assertkVersion"] = "0.28.1"
extra["axonFrameworkVersion"] = "5.1.0-SNAPSHOT"
extra["springDocOpenApiVersion"] = "3.0.2"
extra["springBootVersion"] = "4.0.2"
extra["springAiVersion"] = "2.0.0-M2"


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocOpenApiVersion")}")

    implementation("org.axonframework:axon-eventsourcing:${property("axonFrameworkVersion")}")
    implementation("io.axoniq.framework:axoniq-spring-boot-starter:${property("axonFrameworkVersion")}")
    implementation("io.axoniq.framework:axoniq-spring-boot-autoconfigure:${property("axonFrameworkVersion")}")
    implementation("io.axoniq.framework:axoniq-dead-letter:${property("axonFrameworkVersion")}")
    implementation("org.axonframework.extensions.spring:axon-spring-boot-starter:${property("axonFrameworkVersion")}")
//    implementation("org.axonframework.extensions.spring:axon-spring:${property("axonFrameworkVersion")}")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("tools.jackson.module:jackson-module-kotlin") // jackson 3

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.willowtreeapps.assertk:assertk:${property("assertkVersion")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("io.rest-assured:spring-mock-mvc-kotlin-extensions:6.0.0")
    testImplementation("org.axonframework:axon-test:${property("axonFrameworkVersion")}")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("io.axoniq.framework:axoniq-testcontainer:${property("axonFrameworkVersion")}")

    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
