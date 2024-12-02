plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()
    maven("https://jcenter.bintray.com/")
    mavenLocal()
    maven("https://mvnrepository.com/")
    maven("https://github.com/")
    maven("https://jitpack.io")
    gradlePluginPortal()
}

dependencies {
    implementation("org.lwjgl:lwjgl:3.3.1")
    implementation(kotlin("stdlib"))
    implementation("net.java.dev.jna:jna:5.15.0")
    implementation("org.apache.tika:tika-core:2.6.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("com.badlogicgames.gdx:gdx:1.10.0")
}

application {
    mainClass.set("com.gpustatix.Main")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.gpustatix.Main"
        )
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("GPUStatix-fat")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes("Main-Class" to "com.gpustatix.Main")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}