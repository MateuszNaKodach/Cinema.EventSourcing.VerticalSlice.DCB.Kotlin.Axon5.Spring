plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.10"
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
    mavenCentral()
}

extra["assertkVersion"] = "0.28.1"
extra["axonFrameworkVersion"] = "5.0.0"
extra["springDocOpenApiVersion"] = "2.8.14"
extra["springBootVersion"] = "3.5.8"
extra["springAiVersion"] = "1.1.0"


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.axonframework.extensions.spring:axon-spring-boot-starter:${property("axonFrameworkVersion")}")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springDocOpenApiVersion")}")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")
//    implementation("org.springframework.ai:spring-ai-starter-model-anthropic")
//    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.axonframework:axon-test:${property("axonFrameworkVersion")}")
    testImplementation("com.willowtreeapps.assertk:assertk:${property("assertkVersion")}")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
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
