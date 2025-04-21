plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

configurations {
    create("common")
    getByName("compileClasspath").extendsFrom(getByName("common"))
    getByName("runtimeClasspath").extendsFrom(getByName("common"))
    getByName("developmentNeoForge").extendsFrom(getByName("common"))
}

repositories {
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
}

dependencies {
    "neoForge"("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")

    "common"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    "common"(project(":test-common", configuration = "namedElements")) {
        isTransitive = false
    }
    implementation(project(":neoforge", configuration = "namedElements"))
}