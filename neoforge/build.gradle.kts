plugins {
    id("com.github.johnrengelman.shadow")
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

    // Files in this configuration will be bundled into your mod using the Shadow plugin.
    // Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
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
    "shadowBundle"(project(":common", configuration = "transformProductionNeoForge"))
}

loom {
    mixin {
        useLegacyMixinAp.set(false)
    }
}

tasks.sourcesJar {
    from(project(":common").sourceSets["main"].allSource)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    configurations = listOf(project.configurations.getByName("shadowBundle"))
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
}