plugins {
    alias(libs.plugins.androidlib) apply false
    alias(libs.plugins.kmp) apply false
    alias(libs.plugins.jvm) apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
}