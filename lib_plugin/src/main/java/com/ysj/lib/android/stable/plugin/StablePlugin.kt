package com.ysj.lib.android.stable.plugin

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import androidx.collection.ArraySet
import com.ysj.lib.android.stable.plugin.config.StablePluginConfig
import com.ysj.lib.android.stable.plugin.loader.PluginClassLoader
import com.ysj.lib.android.stable.plugin.loader.PluginHostClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


/**
 * 插件管理器，用于管理插件相关调用。
 *
 * @author Ysj
 * Create time: 2024/9/14
 */
object StablePlugin {

    internal const val STANDARD_ACTIVITY = "com.ysj.lib.android.stable.plugin.StandardActivity"

    internal const val INTENT_KEY_PLUGIN_WRAPPED = "plugin.wrapped"

    private const val TAG = "StablePlugin"

    internal lateinit var config: StablePluginConfig
        private set
    internal lateinit var application: Application
        private set
    internal lateinit var hostPackageInfo: PackageInfo
        private set

    private lateinit var pluginHostClassLoader: PluginHostClassLoader

    private val installedPluginMap = ArrayMap<String, Plugin>()

    private val releaseLockSet = ArraySet<String>()

    private val sdkRootDir get() = File(application.filesDir, "stable_plugin")
    internal val String.pluginInstallDir: File get() = File(sdkRootDir, this)
    internal val String.pluginSoLibDir: File get() = File(pluginInstallDir, "so_lib")
    internal val String.pluginODexDir: File get() = File(pluginInstallDir, "odex")
    internal val String.pluginInstalledFile: File get() = File(pluginInstallDir, "plugin.apk")

    @MainThread
    fun init(application: Application, config: StablePluginConfig) {
        var classLoader = application.classLoader
        if (classLoader is PluginHostClassLoader) {
            // api 28+ 可以通过 AppComponentFactory 机制设置默认的 classLoader
            Log.i(TAG, "init from not hook. sdk-version=${Build.VERSION.SDK_INT}")
        } else @SuppressLint("PrivateApi") {
            classLoader = PluginHostClassLoader(classLoader)
            // 找到 ContextImpl
            var baseContext = application.baseContext
            while (baseContext is ContextWrapper) {
                baseContext = baseContext.baseContext
            }
            // hook LoadedApk 中的 mClassLoader
            val mPackageInfo = Class
                .forName("android.app.ContextImpl")
                .getDeclaredField("mPackageInfo")
                .apply { isAccessible = true }
                .get(baseContext)
            Class.forName("android.app.LoadedApk")
                .getDeclaredField("mClassLoader")
                .apply { isAccessible = true }
                .set(mPackageInfo, classLoader)
            Thread.currentThread().contextClassLoader = classLoader
            // hook 替换 Instrumentation
            val activityThread = Class.forName("android.app.ActivityThread")
                .getDeclaredMethod("currentActivityThread")
                .invoke(null)
            val instrumentationField = Class.forName("android.app.ActivityThread")
                .getDeclaredField("mInstrumentation")
                .apply { isAccessible = true }
            val instrumentation = InstrumentationCompat(classLoader)
            instrumentation.init(instrumentationField.get(activityThread) as Instrumentation)
            instrumentationField.set(activityThread, instrumentation)
            Log.i(TAG, "init from hook. sdk-version=${Build.VERSION.SDK_INT}")
        }
        this.config = config
        this.application = application
        this.pluginHostClassLoader = classLoader
        this.hostPackageInfo = application.packageManager.getPackageInfo(
            application.packageName,
            PackageManager.GET_SHARED_LIBRARY_FILES or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_META_DATA,
        )
        config.eventCallback?.onInitialized()
    }

    /**
     * 释放插件并安装。
     */
    suspend fun installPlugin(pluginName: String, pluginFile: File): Plugin {
        val installed = installedPluginMap[pluginName]
        if (installed != null) {
            return installed
        }
        releasePlugin(pluginName, pluginFile)
        if (!pluginFile.isFile) {
            throw FileNotFoundException(pluginFile.absolutePath)
        }
        val plugin = parseReleasedPlugin(pluginName)
        withContext(Dispatchers.Main.immediate) {
            installedPluginMap[pluginName] = plugin
            pluginHostClassLoader.addPluginClassLoader(plugin.classLoader as PluginClassLoader)
            config.eventCallback?.onPluginInstalled(plugin)
        }
        return plugin
    }

    /**
     * 卸载插件。
     */
    suspend fun uninstallPlugin(pluginName: String): Unit = withContext(Dispatchers.Main.immediate) {
        val plugin = installedPluginMap.remove(pluginName)
        if (plugin != null) {
            pluginHostClassLoader.removePluginClassLoader(plugin.classLoader as PluginClassLoader)
        }
        withContext(Dispatchers.IO) {
            while (isActive) {
                val locked = synchronized(releaseLockSet) {
                    pluginName in releaseLockSet
                }
                if (locked) {
                    delay(50)
                    continue
                }
                break
            }
            pluginName.pluginInstallDir.runCatching { deleteRecursively() }
        }
        if (plugin != null) {
            config.eventCallback?.onPluginUninstalled(plugin)
        }
    }

    /**
     * 释放插件。
     */
    suspend fun releasePlugin(pluginName: String, pluginFile: File): Unit = withContext(Dispatchers.IO) {
        coroutineContext.job.invokeOnCompletion {
            synchronized(releaseLockSet) {
                releaseLockSet.remove(pluginName)
            }
        }
        while (isActive) {
            val locked = synchronized(releaseLockSet) {
                pluginName in releaseLockSet
            }
            if (locked) {
                delay(50)
                continue
            }
            break
        }
        synchronized(releaseLockSet) {
            releaseLockSet.add(pluginName)
        }
        val pluginInstallDir = pluginName.pluginInstallDir
        if (pluginInstallDir.isDirectory) {
            pluginInstallDir.runCatching { deleteRecursively() }
        }
        pluginInstallDir.mkdirs()
        pluginName.pluginODexDir.mkdirs()
        pluginName.pluginSoLibDir.mkdirs()
        ZipFile(pluginFile).use { jf ->
            val soEntries = jf.entries()
                .asSequence()
                .filter { it.name.endsWith(".so") }
                .toList()
            if (soEntries.isEmpty()) {
                return@use
            }
            val abiPrefixList = Build.SUPPORTED_ABIS.map { "lib/${it}" }
            var resultAbiSoEntries: List<ZipEntry>? = null
            for (abiPrefix in abiPrefixList) {
                resultAbiSoEntries = soEntries.filter { it.name.startsWith(abiPrefix) }
                if (resultAbiSoEntries.isNotEmpty()) {
                    break
                }
            }
            if (resultAbiSoEntries.isNullOrEmpty()) {
                return@use
            }
            for (entry in resultAbiSoEntries) {
                val soFileName = entry.name.substringAfterLast("/")
                val soFile = File(pluginName.pluginSoLibDir, soFileName)
                soFile.createNewFile()
                soFile.outputStream().use { ops ->
                    jf.getInputStream(entry).use { ips ->
                        ips.copyTo(ops)
                    }
                }
            }
        }
        // 最后才复制插件 apk，确保到这步时插件已经处于可安装状态
        pluginFile.copyTo(pluginName.pluginInstalledFile)
    }

    /**
     * 安装已经释放到框架中的插件。
     */
    @MainThread
    fun installReleasedPlugin(pluginName: String): Plugin? {
        var plugin = installedPluginMap[pluginName]
        if (plugin != null) {
            return plugin
        }
        if (!checkPluginReleased(pluginName)) {
            return null
        }
        plugin = runCatching { parseReleasedPlugin(pluginName) }
            .getOrNull()
            ?: return null
        installedPluginMap[pluginName] = plugin
        pluginHostClassLoader.addPluginClassLoader(plugin.classLoader as PluginClassLoader)
        config.eventCallback?.onPluginInstalled(plugin)
        return plugin
    }

    /**
     * 获取插件包信息。
     */
    @MainThread
    fun getPluginPackageInfo(pluginName: String): PackageInfo? {
        val plugin = installedPluginMap[pluginName]
        if (plugin != null) {
            return plugin.packageInfo
        }
        if (!checkPluginReleased(pluginName)) {
            return null
        }
        return runCatching { parseReleasedPlugin(pluginName) }.getOrNull()?.packageInfo
    }

    /**
     * 检查插件是否已经在框架中释放。
     *
     * @return if released return true
     */
    @MainThread
    fun checkPluginReleased(pluginName: String): Boolean {
        if (checkPluginInstalled(pluginName)) {
            return true
        }
        synchronized(releaseLockSet) {
            if (pluginName in releaseLockSet) {
                return false
            }
        }
        return pluginName.pluginInstalledFile.isFile
    }

    /**
     * 检查插件是否已经安装。
     *
     * @return if installed return true
     */
    @MainThread
    fun checkPluginInstalled(pluginName: String): Boolean {
        return pluginName in installedPluginMap
    }

    /**
     * 通过插件名获取插件信息。
     */
    @MainThread
    fun findPluginByName(pluginName: String): Plugin? {
        return installedPluginMap[pluginName]
    }

    /**
     * 通过 ClassLoader 查找其所属的插件。
     */
    @MainThread
    fun findPluginByClassLoader(classLoader: ClassLoader): Plugin? {
        return installedPluginMap.values.find {
            it.classLoader === classLoader
        }
    }

    /**
     * 获取所有已经安装的插件。
     */
    @MainThread
    fun allInstalledPlugins(): Collection<Plugin> {
        return installedPluginMap.values
    }

    private fun parseReleasedPlugin(pluginName: String): Plugin {
        val odexDir = pluginName.pluginODexDir
        val solibDir = pluginName.pluginSoLibDir
        val installedPluginFile = pluginName.pluginInstalledFile
        installedPluginFile.setReadOnly()
        val packageInfo: PackageInfo = application.packageManager.getPackageArchiveInfo(
            installedPluginFile.absolutePath,
            PackageManager.GET_SHARED_LIBRARY_FILES or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_META_DATA,
        )!!
        // 设置 apk 文件路径，sourceDir 和 publicSourceDir 在 profileinstaller 中会用到一定得赋值
        packageInfo.applicationInfo.sourceDir = installedPluginFile.absolutePath
        packageInfo.applicationInfo.publicSourceDir = installedPluginFile.absolutePath
        // 设置 so 库的目录
        packageInfo.applicationInfo.nativeLibraryDir = solibDir.absolutePath
        val classLoader = PluginClassLoader(
            pluginPath = installedPluginFile.absolutePath,
            optimizedDirectory = odexDir.absolutePath,
            librarySearchPath = solibDir.absolutePath,
        )
        return Plugin(
            hostApplication = application,
            name = pluginName,
            classLoader = classLoader,
            packageInfo = packageInfo,
        )
    }

}