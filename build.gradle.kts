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

tasks {
    register("kuritests") {
        dependsOn(":kmptestlib:cleanJvmTest")
        dependsOn(":kmptestlib:jvmTest")
    }
    register("publishSnapshot") {
        dependsOn("kuritests")
        dependsOn(":kuri-api:publishAllPublicationsToSonatypeSnapshotRepository")
        dependsOn(":kuri-processor:publishAllPublicationsToSonatypeSnapshotRepository")
    }
    register("publishRelease") {
        dependsOn("kuritests")
        dependsOn(":kuri-api:publishAllPublicationsToSonatypeStagingRepository")
        dependsOn(":kuri-processor:publishAllPublicationsToSonatypeStagingRepository")
    }
}