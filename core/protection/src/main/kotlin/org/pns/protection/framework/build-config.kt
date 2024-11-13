// settings.gradle.kts
rootProject.name = "planetary-nervous-system"
include(":app")
include(":core")

// build.gradle.kts (root)
plugins {
    kotlin("jvm") version "1.9.0" apply false
    kotlin("android") version "1.9.0" apply false
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
}

// core/build.gradle.kts
plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

// app/build.gradle.kts (additions)
dependencies {
    implementation(project(":core"))
}

// gradle/libs.versions.toml
[versions]
kotlin = "1.9.0"
coroutines = "1.7.3"
serialization = "1.5.1"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlinx-coroutines = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-serialization = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }