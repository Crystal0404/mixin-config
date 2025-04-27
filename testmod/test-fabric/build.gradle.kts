plugins {
    id("com.github.johnrengelman.shadow")
    id("net.caffeinemc.mixin-config-plugin").version("1.0-SNAPSHOT")
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
    "common"(project(":test-common", configuration = "namedElements")) {
        isTransitive = false
    }
    "shadowBundle"(project(":test-common", configuration = "transformProductionFabric"))
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

sourceSets.main {
    resources.srcDirs(layout.buildDirectory.dir("fabric-mixin-config-output").get())
}

tasks.named<net.caffeinemc.gradle.CreateMixinConfigTask>("test-fabricCreateMixinConfig") {
    inputFiles.set(
        listOf(
            tasks.named("compileJava", JavaCompile::class).get().destinationDirectory.get(),
            project(":test-common").tasks.named("compileJava", JavaCompile::class).get().destinationDirectory.get()
        )
    )
    includeFiles.set(file("src/main/java/com/example"))
    outputDirectory.set(layout.buildDirectory.dir("fabric-mixin-config-output"))
    outputAssetsPath = "assets/example"
    outputFilenameForSummaryDocument = "example-mod-fabric-mixin-config.md"
    mixinParentPackages = listOf("com.example")
    modShortName = "ExampleMod"

    dependsOn("compileJava")
    dependsOn(project(":common").tasks.named("compileJava", JavaCompile::class))
}

tasks.named("processResources") {
    dependsOn("test-fabricCreateMixinConfig")
}

tasks.named("sourcesJar") {
    dependsOn("test-fabricCreateMixinConfig")
}