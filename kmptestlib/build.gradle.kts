plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidlib)
}

val groupId = "se.ade.kuri.kmpconsumer"
project.group = groupId

kotlin {
    jvm()
    androidTarget()
    iosArm64()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(project(":kuri-api"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.core)
            }
        }
    }

    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

dependencies {
    // Run KSP on [commonMain] code
    add("kspCommonMainMetadata", project(":kuri-processor"))
}

ksp {
    arg("se.ade.kuri.placeholder.start", "{")
    arg("se.ade.kuri.placeholder.end", "}")
}

android {
    compileSdk = androidConfig.versions.compileSdk.get().toInt()
    namespace = groupId
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

dependencies {
    //Adds the ksp processor to commonMain sourceset.
    add("kspCommonMainMetadata", project(":kuri-processor"))
}

tasks.named<Test>("jvmTest") {
    testLogging {
        showExceptions = true
        showStandardStreams = true

        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
        )

        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}