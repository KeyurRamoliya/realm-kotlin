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

package io.realm.kotlin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

// Custom options for POM configurations that might differ between Realm modules
open class PomOptions {
    var name: String = ""
    var description: String = ""
}

// Configure how the Realm module is published
open class RealmPublishExtensions {
    var pom: PomOptions = PomOptions()
    fun pom(action: Action<PomOptions>) {
        action.execute(pom)
    }
}

// Plugin responsible for handling publishing to JitPack
class RealmPublishPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        // JitPack publishing doesn't need to be configured on the root project
        if (project != project.rootProject) {
            configureSubProject(project)
        }
    }

    private fun configureSubProject(project: Project) {
        with(project) {
            plugins.apply(MavenPublishPlugin::class.java)

            extensions.create<RealmPublishExtensions>("realmPublish")

            afterEvaluate {
                project.extensions.getByType<RealmPublishExtensions>().run {
                    configurePom(project, pom)
                }
            }
        }
    }

    private fun configurePom(project: Project, options: PomOptions) {
        project.extensions.getByType<PublishingExtension>().apply {
            // JitPack automatically creates the repository configuration.
            // We only need to define the publications.
            publications.withType<MavenPublication>().all {
                // JitPack uses the GitHub repository URL to determine the groupId and artifactId.
                // The groupId will be 'com.github.YOUR_USERNAME'.
                // The artifactId will be your repository name.
                // You can set these here for local publishing, but JitPack will override them.
                groupId = "com.github.KeyurRamoliya" // <-- IMPORTANT: CHANGE THIS

                pom {
                    name.set(options.name)
                    description.set(options.description)
                    url.set(Realm.projectUrl)
                    licenses {
                        license {
                            name.set(Realm.License.name)
                            url.set(Realm.License.url)
                        }
                    }
                    scm {
                        connection.set(Realm.SCM.connection)
                        developerConnection.set(Realm.SCM.developerConnection)
                        url.set(Realm.SCM.url)
                    }
                    developers {
                        developer {
                            name.set(Realm.Developer.name)
                            email.set(Realm.Developer.email)
                            organization.set(Realm.Developer.organization)
                            organizationUrl.set(Realm.Developer.organizationUrl)
                        }
                    }
                }
            }
        }
    }
}
