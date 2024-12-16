plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidlib)
    id("convention.publish")
}

val groupId = project.properties["groupId"]!!.toString()
group = groupId
version = project.properties["version"]!!.toString()

kotlin {
    jvm()
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    jvmToolchain(17)
}

android {
    compileSdk = androidConfig.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = androidConfig.versions.minSdk.get().toInt()
    }
    namespace = groupId
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}