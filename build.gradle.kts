plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

group = "top.stillmisty"
version = "0.0.1-SNAPSHOT"
description = "XianTao"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/spring") }
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.mybatis.flex.spring.boot4.starter)
    annotationProcessor(libs.mybatis.flex.processor)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.jackson.databind)
    implementation(libs.flyway.database.postgresql)
    compileOnly(libs.lombok)
    developmentOnly(libs.spring.boot.devtools)
    implementation(libs.postgresql)
    annotationProcessor(libs.lombok)
    testImplementation(libs.spring.boot.starter.cache.test)
    testImplementation(libs.spring.boot.starter.flyway.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testRuntimeOnly(libs.junit.platform.launcher)
    implementation(libs.spring.ai.bom)
    implementation(libs.spring.ai.starter.model.openai)
    implementation(libs.simbot.core.spring.boot.starter)
    implementation(libs.simbot.component.onebot.v11.core)
    implementation(libs.ktor.client.cio)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}