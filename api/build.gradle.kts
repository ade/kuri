plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidlib)
    id("maven-publish")
}

val groupId = "se.ade.kuri"
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