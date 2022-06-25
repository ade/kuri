plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidlib)
}

val groupId = "se.ade.kuri"
group = groupId

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