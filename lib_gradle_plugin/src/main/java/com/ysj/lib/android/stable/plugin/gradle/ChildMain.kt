package com.ysj.lib.android.stable.plugin.gradle

import com.android.build.api.artifact.OutOperationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * 用于插件的插件。
 *
 * @author Ysj
 * Create time: 2024/9/18
 */
class ChildMain : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.configure(AndroidComponentsExtension::class.java) { appExt ->
            appExt.onVariants { variant ->
                val copyManifestTask = project.tasks.register("${variant.name}CopyPluginManifest", CopyPluginManifest::class.java) {
                    it.output.set(project.rootProject.layout.buildDirectory.get().dir("manifest"))
                    it.input.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
                }
                val copyApkTask = project.tasks.register("${variant.name}CopyPluginApk", CopyPluginApk::class.java) {
                    it.builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                    it.output.set(project.rootProject.layout.buildDirectory)
                    it.finalizedBy(copyManifestTask.name)
                }
                // 兼容到的低版本（agp 7.4 保持和 bcu 一致），只影响 copyApkTask 是否自动触发
                val hasToListenTo = OutOperationRequest::class
                    .java.methods
                    .find { it.name == "toListenTo" } != null
                if (hasToListenTo) {
                    variant.artifacts
                        .use(copyApkTask)
                        .wiredWith {
                            it.input
                        }
                        .toListenTo(SingleArtifact.APK)
                }
            }
        }
    }

    abstract class CopyPluginManifest : DefaultTask() {

        @get:InputFile
        @get:PathSensitive(PathSensitivity.NONE)
        abstract val input: RegularFileProperty

        @get:OutputDirectory
        abstract val output: DirectoryProperty

        @TaskAction
        fun taskAction() {
            val target = File(output.asFile.get(), "${project.name}-AndroidManifest.xml")
            input.get().asFile.copyTo(target, true)
        }

    }

    abstract class CopyPluginApk : DefaultTask() {

        @get:InputDirectory
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val input: DirectoryProperty

        @get:OutputDirectory
        abstract val output: DirectoryProperty

        @get:Internal
        abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

        @TaskAction
        fun taskAction() {
            val outputDirectory = output.get()

            val builtArtifacts = builtArtifactsLoader.get()
                .load(input.get())
                ?: throw RuntimeException("Cannot load APKs")

            builtArtifacts.elements.forEach { artifact ->
                val target = outputDirectory.file("${project.name}.apk").asFile
                File(artifact.outputFile).copyTo(target, true)
            }
        }
    }
}