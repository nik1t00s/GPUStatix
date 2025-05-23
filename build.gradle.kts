group = "com.gpustatix"
version = "1.0.0"

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()
    maven("https://jcenter.bintray.com/")
    mavenLocal()
    maven {
        url = uri("https://maven.maxhenkel.de/repository/public")
    }
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
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // База данных
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql") // или runtimeOnly("com.h2database:h2")
    // Тестирование
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
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
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    useJUnitPlatform()
}
