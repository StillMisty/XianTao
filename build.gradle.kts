import net.ltgt.gradle.errorprone.errorprone
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spotless)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.version.catalog.update)
}

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        importOrder()
        trimTrailingWhitespace()
        endWithNewline()
    }
    sql {
        target("src/main/resources/db/migration/*.sql")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

group = "top.stillmisty"
version = "0.0.1-SNAPSHOT"
description = "XianTao"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        error("NullAway")
        option("NullAway:AnnotatedPackages", "top.stillmisty.xiantao")
        disable("UnusedVariable")
        excludedPaths.set(".*/build/generated/.*")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/public") }
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.caffeine)
    implementation(libs.aspectj.weaver)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.mybatis.flex.spring.boot4.starter)
    annotationProcessor(libs.mybatis.flex.processor)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.postgresql)
    implementation(libs.flyway.database.postgresql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    developmentOnly(libs.spring.boot.devtools)
    implementation(libs.spring.ai.starter.model.deepseek)
    implementation(libs.spring.ai.starter.model.openai)
    implementation(libs.simbot.core.spring.boot.starter)
    implementation(libs.simbot.component.qq.guild.core)
    implementation(libs.ktor.client.java)
    // NullAway
    implementation(libs.jspecify)
    testImplementation(libs.jspecify)
    annotationProcessor(libs.errorprone.core)
    annotationProcessor(libs.nullaway)
    testAnnotationProcessor(libs.errorprone.core)
    testAnnotationProcessor(libs.nullaway)
    testImplementation(libs.spring.boot.starter.cache.test)
    testImplementation(libs.spring.boot.starter.flyway.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.register<Exec>("installGitHooks") {
    description = "Installs git hooks from gradle/hooks/ into .git/hooks/"
    commandLine(
        "bash", "-c",
        "cp gradle/hooks/* .git/hooks/ && chmod +x .git/hooks/*"
    )
}
