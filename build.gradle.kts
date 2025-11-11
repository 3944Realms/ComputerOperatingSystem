plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "top.r3944realms.superleadrope"
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

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
application {
    mainClass.set("top.r3944realms.cos.MainKt")
}
