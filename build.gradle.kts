plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"

    id("io.freefair.lombok") version "8.10"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.pinodev.it/releases")
    maven("https://jitpack.io")

    maven {
        name = "papi"
        url = uri("https://repo.extendedclip.com/releases/")
    }

}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("it.pino.zelchat:zelchat-api:2.0.0-pre-20.01")

    compileOnly("me.clip:placeholderapi:2.12.3")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}


tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}