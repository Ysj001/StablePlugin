package com.ysj.lib.android.stable.plugin.gradle

import com.ysj.lib.bytecodeutil.plugin.api.IModifier
import com.ysj.lib.bytecodeutil.plugin.api.logger.YLogger
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference

/**
 * 插件 APP 用的字节码修改器。
 *
 * @author Ysj
 * Create time: 2024/9/18
 */
class ChildModifier(
    override val executor: Executor,
    override val allClassNode: Map<String, ClassNode>,
) : IModifier {

    companion object {

        private const val SDK_PACKAGE_NAME = "com/ysj/lib/android/stable/plugin"

        // =============== 原始 ===============
        private const val APPLICATION_CLASS_NAME = "android/app/Application"

        private const val SERVICE_CLASS_NAME = "android/app/Service"

        private const val ACTIVITY_CLASS_NAME = "android/app/Activity"
        private const val ACTIVITY_APP_COMPAT_CLASS_NAME = "androidx/appcompat/app/AppCompatActivity"
        // ===================================

        // =============== 目标 ===============
        private const val PLUGIN_APPLICATION_CLASS_NAME = "$SDK_PACKAGE_NAME/component/PluginApplication"

        private const val PLUGIN_SERVICE_CLASS_NAME = "$SDK_PACKAGE_NAME/component/service/PluginService"

        private const val PLUGIN_ACTIVITY_CLASS_NAME = "$SDK_PACKAGE_NAME/component/activity/PluginActivity"
        private const val PLUGIN_ACTIVITY_APP_COMPAT_CLASS_NAME = "$SDK_PACKAGE_NAME/component/activity/PluginAppCompatActivity"
        // ===================================

        private const val PLUGIN_ACTIVITY_COMPAT_CLASS_NAME = "$SDK_PACKAGE_NAME/component/activity/PluginActivityCompat"

        private const val PLUGIN_PACKAGE_MANAGER_COMPAT_CLASS_NAME = "$SDK_PACKAGE_NAME/component/PluginPackageManagerCompat"

        private const val CONTENT_RESOLVER_PROXY_CLASS_NAME = "$SDK_PACKAGE_NAME/component/provider/PluginContentResolverProxy"

    }

    private val logger = YLogger.getLogger(javaClass)

    private var applicationList = LinkedList<ClassNode>()
    private val serviceList = LinkedList<ClassNode>()
    private val activityList = LinkedList<ClassNode>()
    private val activityAppCompatList = LinkedList<ClassNode>()

    private var contentResolverProxy: ClassNode? = null
    private var pluginPackageManagerCompat: ClassNode? = null

    override fun scan(classNode: ClassNode) {
        if (classNode.name == CONTENT_RESOLVER_PROXY_CLASS_NAME) {
            contentResolverProxy = classNode
            return
        }
        if (classNode.name == PLUGIN_PACKAGE_MANAGER_COMPAT_CLASS_NAME) {
            pluginPackageManagerCompat = classNode
            return
        }
        if (classNode.name.startsWith(SDK_PACKAGE_NAME)) {
            return
        }
        when (classNode.superName) {
            APPLICATION_CLASS_NAME -> synchronized(applicationList) {
                applicationList.add(classNode)
            }
            SERVICE_CLASS_NAME -> synchronized(serviceList) {
                serviceList.add(classNode)
            }
            ACTIVITY_CLASS_NAME -> synchronized(activityList) {
                activityList.add(classNode)
            }
            ACTIVITY_APP_COMPAT_CLASS_NAME -> synchronized(activityAppCompatList) {
                activityAppCompatList.add(classNode)
            }
        }
    }

    override fun modify() {
        val packageManagerMethodMap = genPackageManagerMethodMap()
        val contentResolverMethodMap = genContentResolverMethodMap()
        val latch = CountDownLatch(1 + allClassNode.size)
        val throwable = AtomicReference<Throwable>()
        // 修改组件的父类
        executor.exec(latch, throwable) {
            for (classNode in applicationList) {
                changeSuperClass(classNode, PLUGIN_APPLICATION_CLASS_NAME)
            }
            for (classNode in serviceList) {
                changeSuperClass(classNode, PLUGIN_SERVICE_CLASS_NAME)
            }
            for (classNode in activityAppCompatList) {
                changeSuperClass(classNode, PLUGIN_ACTIVITY_APP_COMPAT_CLASS_NAME)
            }
            for (classNode in activityList) {
                changeSuperClass(classNode, PLUGIN_ACTIVITY_CLASS_NAME)
            }
        }
        for (classNode in allClassNode.values) {
            if (classNode.name.startsWith(SDK_PACKAGE_NAME)) {
                latch.countDown()
                continue
            }
            executor.exec(latch, throwable) {
                for (methodNode in classNode.methods) {
                    synchronized(methodNode) {
                        for (node in methodNode.instructions.toList()) {
                            pluginActivityCompat(classNode, methodNode, node)
                            pluginPackageManagerCompat(packageManagerMethodMap, node)
                            proxyContentResolverInvoke(contentResolverMethodMap, node)
                        }
                    }
                }
            }
        }
        latch.await()
        throwable.get()?.also { throw it }
    }

    private fun pluginActivityCompat(classNode: ClassNode, methodNode: MethodNode, node: AbstractInsnNode) {
        if (node !is MethodInsnNode || !node.owner.isTargetClass(ACTIVITY_CLASS_NAME)) {
            return
        }
        when (node.name) {
            "getApplication" -> {
                if (node.desc != "()Landroid/app/Application;") {
                    return
                }
                logger.info("compatible Activity.getApplication call. in ${classNode.name}-${methodNode.name}")
                node.opcode = Opcodes.INVOKESTATIC
                node.owner = PLUGIN_ACTIVITY_COMPAT_CLASS_NAME
                node.desc = "(Landroid/app/Activity;)Landroid/app/Application;"
            }
        }
    }

    private fun pluginPackageManagerCompat(proxyMethodMap: Map<String, String>, node: AbstractInsnNode) {
        if (node.opcode != Opcodes.INVOKEVIRTUAL || node !is MethodInsnNode) {
            return
        }
        // 代理 PackageManager 的调用
        val key = node.name + node.desc.replace("(", "(Landroid/content/pm/PackageManager;")
        val desc = proxyMethodMap[key] ?: return
        logger.info("compatible PackageManager call. ${node.owner} ${node.name} ${node.desc}")
        node.opcode = Opcodes.INVOKESTATIC
        node.owner = PLUGIN_PACKAGE_MANAGER_COMPAT_CLASS_NAME
        node.desc = desc
    }

    private fun genPackageManagerMethodMap(): Map<String, String> {
        val resolverProxy = requireNotNull(this.pluginPackageManagerCompat) {
            "not found 'PluginPackageManagerCompat' please check your dependencies."
        }
        return resolverProxy.methods
            .asSequence()
            .filter { it.access and Opcodes.ACC_STATIC != 0 }
            .map { it.name + it.desc to it.desc }
            .toMap(HashMap(64))
    }

    private fun proxyContentResolverInvoke(contentResolverProxyMethodMap: Map<String, String>, node: AbstractInsnNode) {
        if (node.opcode != Opcodes.INVOKEVIRTUAL || node !is MethodInsnNode) {
            return
        }
        // 代理 ContentResolver 的调用
        val key = node.name + node.desc.replace("(", "(Landroid/content/ContentResolver;")
        val desc = contentResolverProxyMethodMap[key] ?: return
        logger.info("compatible ContentResolver call. ${node.owner} ${node.name} ${node.desc}")
        node.opcode = Opcodes.INVOKESTATIC
        node.owner = CONTENT_RESOLVER_PROXY_CLASS_NAME
        node.desc = desc
    }

    private fun genContentResolverMethodMap(): Map<String, String> {
        val resolverProxy = requireNotNull(this.contentResolverProxy) {
            "not found 'PluginContentResolverProxy' please check your dependencies."
        }
        return resolverProxy.methods
            .asSequence()
            .filter { it.access and Opcodes.ACC_STATIC != 0 }
            .map { it.name + it.desc to it.desc }
            .toMap(HashMap(64))
    }

    private fun String.isTargetClass(targetClassName: String): Boolean {
        if (this == targetClassName) {
            return true
        }
        return allClassNode[this]?.superName?.isTargetClass(targetClassName) ?: false
    }

    private fun changeSuperClass(classNode: ClassNode, targetSuper: String) {
        val currentSuperName = classNode.superName
        // 替换 class 的继承类
        classNode.superName = targetSuper
        for (methodNode in classNode.methods) {
            synchronized(methodNode) {
                for (node in methodNode.instructions) {
                    // 将方法的 super 调用的 owner 也替换成继承类
                    if (node.opcode == Opcodes.INVOKESPECIAL
                        && node is MethodInsnNode
                        && node.owner == currentSuperName) {
                        node.owner = targetSuper
                        break
                    }
                }
            }
        }
    }

    private fun Executor.exec(latch: CountDownLatch, t: AtomicReference<Throwable>, block: () -> Unit) {
        t.get()?.also { throw it }
        execute {
            if (latch.count == 0L) {
                return@execute
            }
            try {
                block()
                latch.countDown()
            } catch (e: Throwable) {
                t.set(e)
                while (latch.count > 0) {
                    latch.countDown()
                }
            }
        }
    }

}