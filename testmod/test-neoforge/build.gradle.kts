plugins {
    id("com.github.johnrengelman.shadow")
    id("net.caffeinemc.mixin-config-plugin").version("1.0-SNAPSHOT")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

configurations {
    create("common") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    getByName("compileClasspath").extendsFrom(getByName("common"))
    getByName("runtimeClasspath").extendsFrom(getByName("common"))
    getByName("developmentNeoForge").extendsFrom(getByName("common"))

    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
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
    "shadowBundle"(project(":test-common", configuration = "transformProductionFabric"))
    implementation(project(":neoforge", configuration = "namedElements"))

    // mixin config plugin
    compileOnly("net.caffeinemc:mixin-config-plugin:1.0-SNAPSHOT")
}

tasks.shadowJar {
    configurations = listOf(project.configurations.getByName("shadowBundle"))
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
}