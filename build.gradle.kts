/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// buildscript is not needed for this simplified setup.

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting
        val commonTest by getting
    }
}

android {
    compileSdk = 36
    defaultConfig {
        minSdk = 21
        targetSdk = 36
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.github.KeyurRamoliya"
            artifactId = "realm-kotlin"
            version = "4.0.5"
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/KeyurRamoliya/realm-kotlin")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

fun taskName(subdir: String): String {
    return subdir.split("/", "-").joinToString(separator = "") { it.replaceFirstChar { c -> c.uppercaseChar() } }
}

val subprojectPaths = listOf("packages", "examples/kmm-sample", "benchmarks")

tasks {
    // These tasks are for code quality and can remain as they are useful for local
    // development and CI, but are not directly related to publishing.
    register("ktlintCheck") {
        description = "Runs ktlintCheck on all projects."
        group = "Verification"
        dependsOn(subprojectPaths.map { "ktlintCheck${taskName(it)}" })
    }

    register("ktlintFormat") {
        description = "Runs ktlintFormat on all projects."
        group = "Formatting"
        dependsOn(subprojectPaths.map { "ktlintFormat${taskName(it)}" })
    }

    register("detekt") {
        description = "Runs detekt on all projects."
        group = "Verification"
        dependsOn(subprojectPaths.map { "detekt${taskName(it)}" })
    }

    subprojectPaths.forEach { subdir ->
        register<Exec>("ktlintCheck${taskName(subdir)}") {
            description = "Run ktlintCheck on /$subdir project"
            workingDir = file("${rootDir}/$subdir")
        }
    }
}
