plugins {
    id("groovy")
    id("java-library")
    id("kotlin")
}

group = properties["lib.group"] as String
version = properties["lib.version"] as String

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    maven { setUrl("https://maven.aliyun.com/repository/central") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://jitpack.io") }
    google()
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(bcu_plugin_api)
    compileOnly("com.android.tools.build:gradle-api:$ANDROID_GRADLE_VERSION")
}

mavenPublish()