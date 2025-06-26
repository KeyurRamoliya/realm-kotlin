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

// Helper function to capitalize parts of a task name
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
            commandLine = listOf("./gradlew", "ktlintCheck")
        }
        register<Exec>("ktlintFormat${taskName(subdir)}") {
            description = "Run ktlintFormat on /$subdir project"
            workingDir = file("${rootDir}/$subdir")
            commandLine = listOf("./gradlew", "ktlintFormat")
        }
        register<Exec>("detekt${taskName(subdir)}") {
            description = "Run detekt on /$subdir project"
            workingDir = file("${rootDir}/$subdir")
            commandLine = listOf("./gradlew", "detekt")
        }
    }

    // The 'publishToMavenLocal' task is what JitPack will use to build your library.
    // We can create an aggregator task to run it on all relevant subprojects.
    // This makes it easy to test the build locally before pushing to GitHub.
    register("publishToMavenLocal") {
        description = "Publishes all library artifacts to the local Maven repository."
        group = "Publishing"
        // Add dependencies on the publishToMavenLocal tasks of the modules you want to publish.
        // JitPack will automatically find all modules with the 'maven-publish' plugin.
        // This is primarily for local testing.
        dependsOn(subprojectPaths.mapNotNull { subdir ->
            val projectPath = ":${subdir.replace('/', ':')}"
            project(projectPath).tasks.findByName("publishToMavenLocal")
        })
    }
}
