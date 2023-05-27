import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.0"
    id("kotlin")
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
        vendor.set(JvmVendorSpec.ORACLE) // Valhalla build
    }
}

jmh {
    resultsFile.set(project.file("${project.rootDir}/results/jmh/Hotspot-Valhalla.txt"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        languageVersion = "2.0"
        kotlinOptions.freeCompilerArgs = listOf("-Xvalue-classes")
    }
}
