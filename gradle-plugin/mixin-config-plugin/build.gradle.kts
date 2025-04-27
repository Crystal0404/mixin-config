plugins {
    id("java")
    id("java-gradle-plugin")
    id("maven-publish")
}

base {
    archivesName = "mixin-config-plugin"
}

version = "1.0-SNAPSHOT"
group = "net.caffeinemc"

repositories {
    mavenCentral()
}

dependencies {
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("compiler") {
            id = "net.caffeinemc.mixin-config-plugin"
            implementationClass = "net.caffeinemc.gradle.GradleMixinConfigPlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "LICENSE_${project.property("archives_name")}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}