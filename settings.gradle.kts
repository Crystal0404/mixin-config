pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.architectury.dev/") }
        if (System.getenv("CI") != "true" /* not run in github actions */ ) {
            // If you're not from China, please remove this, it will slow down your downloads
            maven {
                url = uri("https://maven.aliyun.com/repository/gradle-plugin")
                content { excludeGroup("me.modmuss50") }
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.neoforged.net/releases") }
    }
}

rootProject.name = "CaffeineConfig"

include("common")
include("fabric")
include("neoforge")

// test
include(":test-common")
include(":test-fabric")
include(":test-neoforge")

project(":test-common").projectDir = file("testmod/test-common")
project(":test-fabric").projectDir = file("testmod/test-fabric")
project(":test-neoforge").projectDir = file("testmod/test-neoforge")