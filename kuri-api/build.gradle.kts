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
    android()
    ios()
}

android {
    compileSdk = androidConfig.versions.compileSdk.get().toInt()
    namespace = groupId
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}