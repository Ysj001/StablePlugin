// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        applyMavenLocal(this)
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://jitpack.io") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:$ANDROID_GRADLE_VERSION")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
        // bcu
        classpath(bcu_plugin)
        classpath(modifier_component_di)
    }
}

subprojects {
    repositories {
        applyMavenLocal(this)
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://jitpack.io") }
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(project.layout.buildDirectory)
}
