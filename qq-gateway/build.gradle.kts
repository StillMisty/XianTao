import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    alias(libs.plugins.spotless)
    alias(libs.plugins.errorprone)
}

description = "QQ 开放平台机器人接入（WebSocket + Webhook）"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(27)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/public") }
}

dependencies {
    // 版本对齐主工程：Jackson / SLF4J / JUnit 均走 Spring Boot BOM
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}"))
    implementation("tools.jackson.core:jackson-databind")
    implementation("org.slf4j:slf4j-api")
    implementation(libs.jspecify)

    annotationProcessor(libs.errorprone.core)
    annotationProcessor(libs.nullaway)

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly(libs.junit.platform.launcher)
    testAnnotationProcessor(libs.errorprone.core)
    testAnnotationProcessor(libs.nullaway)
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        error("NullAway")
        option("NullAway:AnnotatedPackages", "top.stillmisty.qqgateway")
        disable("UnusedVariable")
    }
}

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        importOrder()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
