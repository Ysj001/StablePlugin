plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("bcu-plugin")
    id("android-stable-plugin-host")
}

bcu {
    config {
        loggerLevel = 1
        modifiers = arrayOf(
        )
    }
    filterNot { variant, entryName ->
        !entryName.startsWith("com/ysj/demo")
            && !entryName.startsWith("com/ysj/lib/android/stable/plugin")
            && !entryName.startsWith("androidx")
    }
}

android {
    namespace = "com.ysj.demo.aplugin"
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

    signingConfigs {
        create("common") {
            storeFile = File(rootDir, "test.jks")
            keyAlias = "key0"
            storePassword = "123456"
            keyPassword = "123456"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("common")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("common")
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
        additionalParameters += listOf("--keep-raw-values")
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
    implementation("androidx.startup:startup-runtime:1.2.0")
    implementation("androidx.profileinstaller:profileinstaller:1.4.0")
//    implementation("com.qihoo360.replugin:replugin-host-lib:2.2.4")
}