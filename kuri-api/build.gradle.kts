plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidlib)
    id("convention.publication")
}

val groupId = project.properties["groupId"]!!.toString()
group = groupId
version = project.properties["version"]!!.toString()

kotlin {
    jvm()
    androidTarget()
    iosX64()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    namespace = groupId
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}