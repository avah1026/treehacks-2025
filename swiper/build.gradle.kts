plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
}

group = "tech.pacia"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "tech.pacia.MainKt"
}

kotlin {
    jvmToolchain(23)
}

dependencies {
    implementation("dev.mobile:maestro-client:1.39.13")
    implementation("dev.mobile:maestro-utils:1.39.13") // required because Metrics
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    implementation("io.ktor:ktor-server-core-jvm:3.1.0")
    implementation("io.ktor:ktor-server-cio:3.1.0") // don't use netty, it breaks Maestro's gRPC :(
    // implementation("io.ktor:ktor-server-netty:3.1.0") // don't use netty, it breaks Maestro's gRPC :(
    implementation("dev.mobile:maestro-orchestra:1.39.13")
    implementation("dev.mobile:maestro-ios:1.39.13") // required because has SLF4J?

    implementation("ch.qos.logback:logback-classic:1.5.16")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
