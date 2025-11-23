plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "top.r3944realms.cos"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // YAML 处理依赖
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")

    testImplementation(kotlin("test"))
}

tasks.shadowJar {
    archiveBaseName.set("${project.name}-shadow")
    archiveVersion.set("")
    archiveClassifier.set("")
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

kotlin {
    jvmToolchain(17)
}
application {
    mainClass.set("top.r3944realms.cos.MainKt")
}
