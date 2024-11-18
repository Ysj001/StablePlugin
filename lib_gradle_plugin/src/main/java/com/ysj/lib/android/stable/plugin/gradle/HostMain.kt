package com.ysj.lib.android.stable.plugin.gradle

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * 用于宿主的插件。
 *
 * @author Ysj
 * Create time: 2024/9/18
 */
class HostMain : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.configure(AndroidComponentsExtension::class.java) { appExt ->
            appExt.onVariants { variant ->
                val manifestTransformTask = project.tasks.register(
                    "${variant.name}HostManifestTransformTask",
                    HostManifestTransformTask::class.java,
                )
                manifestTransformTask.configure {
                    val manifestDirPath = project.properties["plugin.manifest.dir"] as String?
                    if (manifestDirPath.isNullOrEmpty()) {
                        it.inputPluginManifests.set(
                            project.rootProject
                                .layout.buildDirectory
                                .dir("manifest")
                                .get()
                        )
                    } else {
                        it.inputPluginManifests.set(File(manifestDirPath))
                    }
                }
                variant.artifacts
                    .use(manifestTransformTask)
                    .wiredWithFiles(
                        HostManifestTransformTask::inputManifest,
                        HostManifestTransformTask::outputManifest,
                    )
                    .toTransform(SingleArtifact.MERGED_MANIFEST)
            }
        }
    }

}