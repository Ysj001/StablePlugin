package com.ysj.lib.android.stable.plugin.gradle

import com.ysj.lib.bytecodeutil.plugin.api.IModifier
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
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

        private const val ACTIVITY_CLASS_NAME = "android/app/Activity"
        private const val ACTIVITY_APP_COMPAT_CLASS_NAME = "androidx/appcompat/app/AppCompatActivity"
        // ===================================

        // =============== 目标 ===============
        private const val PLUGIN_APPLICATION_CLASS_NAME = "$SDK_PACKAGE_NAME/component/PluginApplication"

        private const val PLUGIN_ACTIVITY_CLASS_NAME = "$SDK_PACKAGE_NAME/component/activity/PluginActivity"
        private const val PLUGIN_ACTIVITY_APP_COMPAT_CLASS_NAME = "$SDK_PACKAGE_NAME/component/activity/PluginAppCompatActivity"
        // ===================================

        private const val CONTENT_RESOLVER_PROXY_CLASS_NAME = "$SDK_PACKAGE_NAME/component/provider/PluginContentResolverProxy"
    }

    private var applicationList = LinkedList<ClassNode>()
    private val activityList = LinkedList<ClassNode>()
    private val activityAppCompatList = LinkedList<ClassNode>()

    private var contentResolverProxy: ClassNode? = null

    override fun scan(classNode: ClassNode) {
        if (classNode.name == CONTENT_RESOLVER_PROXY_CLASS_NAME) {
            contentResolverProxy = classNode
            return
        }
        if (classNode.name.startsWith(SDK_PACKAGE_NAME)) {
            return
        }
        when (classNode.superName) {
            APPLICATION_CLASS_NAME -> {
                applicationList.add(classNode)
            }
            ACTIVITY_CLASS_NAME -> {
                activityList.add(classNode)
            }
            ACTIVITY_APP_COMPAT_CLASS_NAME -> {
                activityAppCompatList.add(classNode)
            }
        }
    }

    override fun modify() {
        val contentResolverProxy = requireNotNull(this.contentResolverProxy) {
            "not found 'PluginContentResolverProxy' please check your dependencies."
        }
        val contentResolverProxyMethodMap = contentResolverProxy.methods
            .asSequence()
            .filter { it.access and Opcodes.ACC_STATIC != 0 }
            .map { it.name + it.desc to it.desc }
            .toMap(HashMap())
        val latch = CountDownLatch(1 + allClassNode.size)
        val throwable = AtomicReference<Throwable>()
        // 修改组件的父类
        executor.exec(latch, throwable) {
            for (classNode in applicationList) {
                changeSuperClass(classNode, PLUGIN_APPLICATION_CLASS_NAME)
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
                        for (node in methodNode.instructions) {
                            if (node.opcode != Opcodes.INVOKEVIRTUAL || node !is MethodInsnNode) {
                                continue
                            }
                            val key = node.name + node.desc.replace("(", "(Landroid/content/ContentResolver;")
                            val desc = contentResolverProxyMethodMap[key] ?: continue
                            node.opcode = Opcodes.INVOKESTATIC
                            node.owner = CONTENT_RESOLVER_PROXY_CLASS_NAME
                            node.desc = desc
                        }
                    }
                }
            }
        }
        // 代理
        latch.await()
        throwable.get()?.also { throw it }
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