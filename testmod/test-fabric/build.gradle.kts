plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    create("common")
    getByName("compileClasspath").extendsFrom(getByName("common"))
    getByName("runtimeClasspath").extendsFrom(getByName("common"))
    getByName("developmentFabric").extendsFrom(getByName("common"))
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "Terraformers"
        url = uri("https://maven.terraformersmc.com/")
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")

    "common"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    "common"(project(":test-common", configuration = "namedElements")) {
        isTransitive = false
    }
    implementation(project(":fabric", configuration = "namedElements"))

    // modmenu
    modRuntimeOnly("com.terraformersmc:modmenu:${rootProject.property("modmenu_version")}")

    val apiModules = setOf(
        "fabric-resource-loader-v0",
        "fabric-screen-api-v1",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1"
    )

    apiModules.forEach {
        modRuntimeOnly(fabricApi.module(it, "${rootProject.property("fabric_api_version")}"))
    }
}