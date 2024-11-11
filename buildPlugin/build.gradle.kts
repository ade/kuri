plugins {
    `kotlin-dsl`
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.kmp) apply false
}

kotlin {
    jvmToolchain(17)
}

repositories {
    gradlePluginPortal() // To use 'maven-publish' and 'signing' plugins in our own plugin
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(libs.gradle.maven.publish.plugin)
}