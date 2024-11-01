plugins {
    id("java")
    id("application")
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
    implementation("org.openjfx:javafx-graphics:20")
    implementation("org.openjfx:javafx-base:20")
    implementation("org.openjfx:javafx-controls:19")
    implementation("org.openjfx:javafx-fxml:19")
    implementation(kotlin("stdlib"))
    implementation("net.java.dev.jna:jna:5.10.0")
    implementation("com.github.oshi:oshi-core:6.4.2")
    implementation("org.apache.tika:tika-core:2.6.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
