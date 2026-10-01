pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/spring") }
    }
}

rootProject.name = "XianTao"

// QQ 开放平台接入模块（WebSocket + Webhook），纯 Java 实现，不依赖 SimBot/Kotlin
include("qq-gateway")