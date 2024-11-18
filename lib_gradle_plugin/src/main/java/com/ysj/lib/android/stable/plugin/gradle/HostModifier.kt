package com.ysj.lib.android.stable.plugin.gradle

import com.ysj.lib.bytecodeutil.plugin.api.IModifier
import org.objectweb.asm.tree.ClassNode
import java.util.concurrent.Executor

/**
 * 宿主 APP 用的字节码修改器。
 *
 * @author Ysj
 * Create time: 2024/9/23
 */
class HostModifier(
    override val executor: Executor,
    override val allClassNode: Map<String, ClassNode>,
) : IModifier {

    private var hostClassLoader: ClassNode? = null

    override fun scan(classNode: ClassNode) {

    }

    override fun modify() {

    }

}