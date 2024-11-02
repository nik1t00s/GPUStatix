plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
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
    implementation("org.lwjgl:lwjgl:3.3.0")
    implementation("org.openjfx:javafx-graphics:23")
    implementation("org.openjfx:javafx-base:23")
    implementation("org.openjfx:javafx-controls:23")
    implementation("org.openjfx:javafx-fxml:23")
    implementation(kotlin("stdlib"))
    implementation("net.java.dev.jna:jna:5.10.0")
    implementation("com.github.oshi:oshi-core:6.4.2")
    implementation("org.apache.tika:tika-core:2.6.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("com.badlogicgames.gdx:gdx:1.10.0")
}

application {
    mainClass.set("com.gpustatix.Main")
    applicationDefaultJvmArgs = listOf(
        "--module-path", "/Users/jabka125/Downloads/javafx-sdk-23/lib",
        "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base"
    )
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.gpustatix.Main"
        )
    }
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--module-path", "/Users/jabka125/Downloads/javafx-sdk-23/lib",
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

javafx {
    version = "23"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base")
}
