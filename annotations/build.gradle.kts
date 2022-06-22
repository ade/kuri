plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

group = "se.ade.kuri"

kotlin {
    jvm()
    android()
    ios()
}

android {
    compileSdk = 31
    namespace = "se.ade.kuri"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}