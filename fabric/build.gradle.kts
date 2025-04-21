plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    create("common") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    getByName("compileClasspath").extendsFrom(getByName("common"))
    getByName("runtimeClasspath").extendsFrom(getByName("common"))
    getByName("developmentFabric").extendsFrom(getByName("common"))

    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
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
    "shadowBundle"(project(":common", configuration = "transformProductionFabric"))

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

    filesMatching("fabric.mod.json") {
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

publishMods {
    val isEnableDebug = providers.environmentVariable("BUILD_RELEASE").orNull == null
    dryRun = isEnableDebug

    file = tasks.remapJar.get().archiveFile
    additionalFiles.from(tasks.sourcesJar.get().archiveFile)
    changelog = if (isEnableDebug) "## Test" else providers.environmentVariable("CHANGELOG").toString()
    val modVersion = "${rootProject.property("mod_version")}"
    version = "v$modVersion-fabric"
    displayName = "CaffeineConfig v$modVersion for Fabric"
    type = when {
        modVersion.endsWith("-alpha") -> ALPHA
        modVersion.endsWith("-beta") -> BETA
        else -> STABLE
    }
    modLoaders.add("fabric")

//    modrinth {
//        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
//        projectId = "123456"
//        minecraftVersionRange {
//            start = "1.14.4"
//            end = "latest"
//        }
//    }
}