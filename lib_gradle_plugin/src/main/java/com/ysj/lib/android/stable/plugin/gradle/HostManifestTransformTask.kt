package com.ysj.lib.android.stable.plugin.gradle

import groovy.namespace.QName
import groovy.util.Node
import groovy.xml.Namespace
import groovy.xml.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * 用于处理宿主的 AndroidManifest.xml。
 *
 * @author Ysj
 * Create time: 2024/9/18
 */
abstract class HostManifestTransformTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputManifest: RegularFileProperty

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    @get:InputDirectory
    abstract val inputPluginManifests: DirectoryProperty

    private val androidNamespace = Namespace("http://schemas.android.com/apk/res/android")

    private val androidName = androidNamespace.get("name")
    private val androidTheme = androidNamespace.get("theme")
    private val androidProcess = androidNamespace.get("process")
    private val androidExported = androidNamespace.get("exported")
    private val androidLabel = androidNamespace.get("label")
    private val androidLogo = androidNamespace.get("logo")
    private val androidIcon = androidNamespace.get("icon")
    private val androidRoundIcon = androidNamespace.get("roundIcon")
    private val androidAuthorities = androidNamespace.get("authorities")

    private val `user-permission` = QName.valueOf("uses-permission")
    private val application = QName.valueOf("application")

    private val activity = QName.valueOf("activity")
    private val `activity-alias` = QName.valueOf("activity-alias")
    private val service = QName.valueOf("service")
    private val receiver = QName.valueOf("receiver")
    private val provider = QName.valueOf("provider")
    private val `meta-data` = QName.valueOf("meta-data")

    private val action = QName.valueOf("action")
    private val category = QName.valueOf("category")
    private val `intent-filter` = QName.valueOf("intent-filter")

    @TaskAction
    fun taskAction() {
        val inputFile = inputManifest.get().asFile
        val outputFile = outputManifest.get().asFile
        val inputPluginManifestDir = inputPluginManifests.get().asFile
        val pluginManifestFileList = inputPluginManifestDir
            .walk()
            .maxDepth(1)
            .filter { it.isFile }
            .toList()
        if (pluginManifestFileList.isEmpty()) {
            return
        }
        // 读取 host 的 Manifest
        val hostRootNode = XmlParser().parse(inputFile)
        val hostApplicationNode = hostRootNode.getAt(application).first() as Node
        val hostUserPermissionsSet = hostRootNode.getAt(`user-permission`)
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.attribute(androidName) }
            .toMutableSet()
        for (manifestFile in pluginManifestFileList) {
            logger.lifecycle("process plugin. ${manifestFile.name}")
            val rootNode = XmlParser().parse(manifestFile)
            for (pluginUserPermission in rootNode.getAt(`user-permission`)) {
                if (pluginUserPermission is Node) {
                    val name = pluginUserPermission.attribute(androidName)
                    if (name !in hostUserPermissionsSet) {
                        hostUserPermissionsSet.add(name)
                        hostRootNode.append(pluginUserPermission)
                        logger.lifecycle("insert plugin permission. $name")
                    }
                }
            }
            val pluginApplicationNode = rootNode
                .getAt(application)
                .firstOrNull()
                ?: continue
            pluginApplicationNode as Node
//            processMetadata(hostApplicationNode, pluginApplicationNode)
            processActivity(hostApplicationNode, pluginApplicationNode)
            processActivityAlias(hostApplicationNode, pluginApplicationNode)
            processService(hostApplicationNode, pluginApplicationNode)
            processReceiver(hostApplicationNode, pluginApplicationNode)
//            processProvider(hostApplicationNode, pluginApplicationNode)
        }
        if (outputFile.isFile) {
            outputFile.delete()
        }
        outputFile.createNewFile()
        outputFile.outputStream().use {
            XmlUtil.serialize(hostRootNode, it)
        }
    }

    private fun processProvider(hostApplicationNode: Node, pluginApplicationNode: Node) {
        val hostProviderSet = hostApplicationNode
            .getAt(provider)
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.attribute(androidName) }
            .toSet()
        val hostAppId = hostApplicationNode.parent().attribute("package").toString()
        val pluginAppId = pluginApplicationNode.parent().attribute("package").toString()
        for (providerNode in pluginApplicationNode.getAt(provider)) {
            if (providerNode !is Node) {
                continue
            }
            val name = providerNode.attribute(androidName)
            if (name in hostProviderSet) {
                continue
            }
            val authorities = providerNode.attribute(androidAuthorities)?.toString()
            if (authorities != null) {
                providerNode.attributes()[androidAuthorities] =
                    authorities.replace(pluginAppId, hostAppId)
            }
            hostApplicationNode.append(providerNode)
        }
    }

    private fun processReceiver(hostApplicationNode: Node, pluginApplicationNode: Node) {
        val hostReceiverSet = hostApplicationNode
            .getAt(receiver)
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.attribute(androidName) }
            .toSet()
        for (receiverNode in pluginApplicationNode.getAt(receiver)) {
            if (receiverNode !is Node) {
                continue
            }
            if (receiverNode.attribute(androidName) in hostReceiverSet) {
                continue
            }
            hostApplicationNode.append(receiverNode)
            logger.lifecycle("insert plugin receiver. ${receiverNode.attribute(androidName)}")
        }
    }

    private fun processService(hostApplicationNode: Node, pluginApplicationNode: Node) {
        val hostServiceSet = hostApplicationNode
            .getAt(service)
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.attribute(androidName) }
            .toSet()
        for (serviceNode in pluginApplicationNode.getAt(service)) {
            if (serviceNode !is Node) {
                continue
            }
            if (serviceNode.attribute(androidName) in hostServiceSet) {
                continue
            }
            hostApplicationNode.append(serviceNode)
            logger.lifecycle("insert plugin service. ${serviceNode.attribute(androidName)}")
        }
    }

    private fun processActivityAlias(hostApplicationNode: Node, pluginApplicationNode: Node) {
        val hostActivityAliasSet = hostApplicationNode
            .getAt(`activity-alias`)
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.attribute(androidName) }
            .toSet()
        for (activityAliasNode in pluginApplicationNode.getAt(`activity-alias`)) {
            if (activityAliasNode !is Node) {
                continue
            }
            if (activityAliasNode.attribute(androidName) in hostActivityAliasSet) {
                continue
            }
            activityAliasNode.attributes().remove(androidTheme)
            activityAliasNode.attributes().remove(androidLabel)
            activityAliasNode.attributes().remove(androidLogo)
            activityAliasNode.attributes().remove(androidIcon)
            activityAliasNode.attributes().remove(androidRoundIcon)
            hostApplicationNode.append(activityAliasNode)
            logger.lifecycle("insert plugin activity alias. ${activityAliasNode.attribute(androidName)}")
        }
    }

    private fun processActivity(hostApplicationNode: Node, pluginApplicationNode: Node) {
        val hostActivitySet = hostApplicationNode
            .getAt(activity)
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.attribute(androidName) }
            .toSet()
        for (activityNode in pluginApplicationNode.getAt(activity)) {
            if (activityNode !is Node) {
                continue
            }
            if (activityNode.attribute(androidName) in hostActivitySet) {
                continue
            }
            activityNode.attributes().remove(androidTheme)
            activityNode.attributes().remove(androidLabel)
            activityNode.attributes().remove(androidLogo)
            activityNode.attributes().remove(androidIcon)
            activityNode.attributes().remove(androidRoundIcon)
            if (activityNode.attribute(androidExported) == null) {
                // 如果没设置 exported 要加上
                activityNode.attributes()[androidExported] = "true"
            }
            val intentFilter = activityNode.getAt(`intent-filter`).firstOrNull()
            if (intentFilter is Node) {
                for (action in intentFilter.getAt(action)) {
                    if (action !is Node) {
                        continue
                    }
                    if (action.attribute(androidName) == "android.intent.action.MAIN") {
                        // 只有 host 能带，插件不能带这个
                        intentFilter.remove(action)
                    }
                }
                for (category in intentFilter.getAt(category)) {
                    if (category !is Node) {
                        continue
                    }
                    if (category.attribute(androidName) == "android.intent.category.LAUNCHER") {
                        // 只有 host 能带，插件不能带这个
                        intentFilter.remove(category)
                    }
                }
            }
            hostApplicationNode.append(activityNode)
            logger.lifecycle("insert plugin activity. ${activityNode.attribute(androidName)}")
        }
    }

    private fun processMetadata(hostApplicationNode: Node, pluginApplicationNode: Node) {
        val hostMetadataSet = hostApplicationNode
            .getAt(`meta-data`)
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.attribute(androidName) }
            .toSet()
        for (metadataNode in pluginApplicationNode.getAt(`meta-data`)) {
            if (metadataNode !is Node) {
                continue
            }
            if (metadataNode.attribute(androidName) in hostMetadataSet) {
                continue
            }
            hostApplicationNode.append(metadataNode)
            logger.lifecycle("insert plugin meta-data. ${metadataNode.attribute(androidName)}")
        }
    }

}