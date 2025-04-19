plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("bcu-plugin")
    id("android-stable-plugin-child")
}

bcu {
    config {
        loggerLevel = 1
        modifiers = arrayOf(
            com.ysj.lib.android.stable.plugin.gradle.ChildModifier::class.java
        )
    }
    filterNot { variant, entryName ->
        entryName.startsWith("kotlin")
            || entryName.startsWith("org")
            || entryName.startsWith("okio")
            || entryName.startsWith("okhttp")
            || entryName.startsWith("retrofit")
            || entryName.startsWith("net")
    }
}

android {
    namespace = "com.ysj.demo.aplugin.demo1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ysj.demo.aplugin"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
//            abiFilters += "armeabi-v7a"
//            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x20")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // minSdk >= 23 会关闭 so 文件压缩，这里重新开启
            useLegacyPackaging = true
        }
    }
    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    applyAndroidTest()
    applyKotlin()
    applyAndroidKtx()
    applyAndroidCommon()
    implementation(project(":lib_plugin"))
    implementation(project(":demo_plugin1:api"))
    implementation(modifier_component_di_api)
}