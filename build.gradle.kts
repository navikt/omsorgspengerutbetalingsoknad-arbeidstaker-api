import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val dusseldorfKtorVersion = "3.1.6.7-f942a29"
val ktorVersion = ext.get("ktorVersion").toString()
val mainClass = "no.nav.omsorgspengerutbetaling.AppKt"
val lettuceVersion = "5.2.2.RELEASE"
val k9FormatVersion = "5.7.2"
val fuelVersion = "2.3.1"
val kafkaEmbeddedEnvVersion = ext.get("kafkaEmbeddedEnvVersion").toString()
val kafkaVersion = ext.get("kafkaVersion").toString() // Alligned med version fra kafka-embedded-env

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

buildscript {
    // Henter ut diverse dependency versjoner, i.e. ktorVersion.
    apply("https://raw.githubusercontent.com/navikt/dusseldorf-ktor/f942a29fd1007215e0254b7d1a83fbc7a3fe4c21/gradle/dusseldorf-ktor.gradle.kts")
}

dependencies {
    // Server
    implementation ( "no.nav.helse:dusseldorf-ktor-core:$dusseldorfKtorVersion")
    implementation ( "no.nav.helse:dusseldorf-ktor-jackson:$dusseldorfKtorVersion")
    implementation ( "no.nav.helse:dusseldorf-ktor-metrics:$dusseldorfKtorVersion")
    implementation ( "no.nav.helse:dusseldorf-ktor-health:$dusseldorfKtorVersion")
    implementation ( "no.nav.helse:dusseldorf-ktor-auth:$dusseldorfKtorVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion"){
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }

    // Client
    implementation ( "no.nav.helse:dusseldorf-ktor-client:$dusseldorfKtorVersion")
    implementation ( "no.nav.helse:dusseldorf-oauth2-client:$dusseldorfKtorVersion")

    // Redis
    implementation ("io.lettuce:lettuce-core:$lettuceVersion")

    // kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

    //K9-format
    implementation("no.nav.k9:soknad:$k9FormatVersion")
    implementation("org.glassfish:jakarta.el:3.0.4")

    // Test
    testImplementation("com.github.fppt:jedis-mock:1.0.1")
    testImplementation("no.nav.helse:dusseldorf-test-support:$dusseldorfKtorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation ("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.1.1")
    testImplementation("no.nav:kafka-embedded-env:$kafkaEmbeddedEnvVersion")
    testImplementation("io.mockk:mockk:1.12.2")
}

repositories {
    mavenLocal()

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/navikt/dusseldorf-ktor")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    mavenCentral()
    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    manifest {
        attributes(
                mapOf(
                        "Main-Class" to mainClass
                )
        )
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.3.3"
}

tasks.withType<Test> {
    useJUnitPlatform()
}