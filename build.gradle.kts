plugins {
    id("java")
    id("application")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.lwjgl:lwjgl:3.3.0")
    implementation("net.java.dev.jna:jna:5.10.0")
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
