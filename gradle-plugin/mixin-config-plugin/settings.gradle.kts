pluginManagement {
    repositories {
        if (System.getenv("CI") != "true" /* not run in github actions */ ) {
            // If you're not from China, please remove this, it will slow down your downloads
            maven {
                url = uri("https://maven.aliyun.com/repository/gradle-plugin")
                content { excludeGroup("me.modmuss50") }
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "mixin-config-plugin"