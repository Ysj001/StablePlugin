import org.gradle.api.artifacts.dsl.DependencyHandler

/*
 * 依赖配置
 *
 * @author Ysj
 * Create time: 2024/6/4
 */

// ================ bcu 相关 =================
private const val BCU_VERSION = "2.1.2"
const val bcu_plugin = "com.github.Ysj001.BytecodeUtil:plugin:$BCU_VERSION"
const val bcu_plugin_api = "com.github.Ysj001.BytecodeUtil:plugin-api:$BCU_VERSION"
const val modifier_aspect = "com.github.Ysj001.bcu-modifier-aspect:modifier-aspect:1.0.0-beta2"
const val modifier_aspect_api = "com.github.Ysj001.bcu-modifier-aspect:modifier-aspect-api:1.0.0-beta2"
const val modifier_component_di = "com.github.Ysj001.bcu-modifier-component-di:modifier-component-di:1.0.1"
const val modifier_component_di_api = "com.github.Ysj001.bcu-modifier-component-di:modifier-component-di-api:1.0.1"
// ==========================================

// ================ czm 相关 ==================
const val czm_lib_core = "com.czm.lib:lib_core:1.1.0"
const val czm_lib_album = "com.czm.lib:lib_album:1.0.7"
const val czm_lib_downloader = "com.czm.lib:lib_downloader:1.1.1"
// ==========================================

fun DependencyHandler.applyKotlin(configName: String = "implementation") {
//    implementation platform('org.jetbrains.kotlin:kotlin-bom:1.8.0')
    add(configName, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    add(configName, "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

fun DependencyHandler.applyAndroidTest() {
    add("testImplementation", "junit:junit:4.13.2")
    add("androidTestImplementation", "androidx.test.ext:junit:1.1.5")
    add("androidTestImplementation", "androidx.test.ext:junit-ktx:1.1.5")
    add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.5.1")
}

fun DependencyHandler.applyAndroidCommon(configName: String = "implementation") {
    add(configName, "androidx.appcompat:appcompat:1.7.0")
    add(configName, "com.google.android.material:material:1.12.0")
    add(configName, "androidx.constraintlayout:constraintlayout:2.1.4")
    add(configName, "androidx.recyclerview:recyclerview:1.3.2")
    add(configName, "androidx.viewpager2:viewpager2:1.1.0")
}

fun DependencyHandler.applyAndroidKtx(configName: String = "implementation") {
    add(configName, "androidx.core:core-ktx:1.13.1")
    add(configName, "androidx.activity:activity-ktx:1.9.2")
    add(configName, "androidx.fragment:fragment-ktx:1.8.3")
    val lifecycleVersion = "2.8.5"
    add(configName, "androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion")
    add(configName, "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    add(configName, "androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycleVersion")
    add(configName, "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    add(configName, "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
}

// ==================== Jetpack ======================

private const val NAVIGATION_VERSION = "2.7.4"

fun DependencyHandler.applyAndroidNavigationPlugin() {
    add("classpath", "androidx.navigation:navigation-safe-args-gradle-plugin:$NAVIGATION_VERSION")
}

fun DependencyHandler.applyAndroidNavigation(configName: String = "implementation") {
    add(configName, "androidx.navigation:navigation-fragment-ktx:$NAVIGATION_VERSION")
    add(configName, "androidx.navigation:navigation-ui-ktx:$NAVIGATION_VERSION")
}

private const val MEDIA3_VERSION = "1.1.1"

const val meida3_common = "androidx.media3:media3-common:$MEDIA3_VERSION"
const val meida3_exoplayer = "androidx.media3:media3-exoplayer:$MEDIA3_VERSION"
const val meida3_ui = "androidx.media3:media3-ui:$MEDIA3_VERSION"
const val meida3_session = "androidx.media3:media3-session:$MEDIA3_VERSION"

// ======================= 下面是第三方 =====================

// Github Ysj001
const val media_codec = "com.github.Ysj001:media-codec:1.0.0-beta"

// glide
const val glide = "com.github.bumptech.glide:glide:4.16.0"
const val glide_compiler = "com.github.bumptech.glide:compiler:4.16.0"
const val glide_webp = "com.github.zjupure:webpdecoder:2.3.4.14.2"

// okhttp
private const val OKHTTP_VERSION = "4.12.0"
const val okhttp = "com.squareup.okhttp3:okhttp:$OKHTTP_VERSION"
const val okhttp_sse = "com.squareup.okhttp3:okhttp-sse:$OKHTTP_VERSION"
const val okhttp_logging = "com.squareup.okhttp3:logging-interceptor:$OKHTTP_VERSION"

// retrofit
private const val RETROFIT_VERSION = "2.9.0"
const val retrofit = "com.squareup.retrofit2:retrofit:$RETROFIT_VERSION"
const val retrofit_converter_gson = "com.squareup.retrofit2:converter-gson:$RETROFIT_VERSION"

// gson
const val gson = "com.google.code.gson:gson:2.10.1"

fun DependencyHandler.applyAutoService(configName: String = "implementation") {
    val version = "1.1.1"
    add(configName, "com.google.auto.service:auto-service:$version")
    add("kapt", "com.google.auto.service:auto-service-annotations:$version")
}