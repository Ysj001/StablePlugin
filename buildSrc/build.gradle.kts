plugins {
    `kotlin-dsl`
}

private val reposDir = File(rootDir, "../repos")

repositories {
    maven { url = reposDir.toURI() }
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/central") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://jitpack.io") }
    google()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation("com.android.tools.build:gradle-api:8.4.1")
    val properties = org.jetbrains.kotlin
        .konan.properties
        .loadProperties(File(rootDir, "../gradle.properties").absolutePath)
    val hasPlugin = reposDir.run {
        isDirectory && !list().isNullOrEmpty()
    }
    if (hasPlugin) {
        val libGroup = properties["lib.group"] as String
        val libVersion = properties["lib.version"] as String
        implementation("$libGroup:stable-plugin-gradle:$libVersion")
    }
}