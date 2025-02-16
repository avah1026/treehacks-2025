plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

group = "tech.pacia.tinderswiper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "tech.pacia.tinderswiper.MainKt"
}

kotlin {
    jvmToolchain(23)
}

dependencies {
    implementation("dev.mobile:maestro-client:1.39.13")
    implementation("dev.mobile:maestro-utils:1.39.13") // required because Metrics
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    implementation("io.ktor:ktor-server-core-jvm:3.1.0")
    implementation("io.ktor:ktor-server-cio:3.1.0") // use CIO instead of Netty. Netty gets duplicated and breaks Maestro's gRPC :(
    // implementation("io.ktor:ktor-server-netty:3.1.0") // don't use netty, it breaks Maestro's gRPC :(
    // implementation("dev.mobile:maestro-orchestra:1.39.13")
    implementation("dev.mobile:maestro-ios:1.39.13") // required because has SLF4J?

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
    // implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("io.ktor:ktor-server-cors:3.1.0")
    implementation("dev.shreyaspatil.generativeai:generativeai-google:0.9.0-1.0.1")
    // implementation("com.google.generativeai:generativeai:0.1.1")

    implementation("ch.qos.logback:logback-classic:1.5.16")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
