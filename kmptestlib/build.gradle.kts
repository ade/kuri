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
    iosX64()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

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
                implementation("io.kotest:kotest-assertions-core:5.1.0")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

ksp {
    arg("se.ade.kuri.placeholder.start", "{")
    arg("se.ade.kuri.placeholder.end", "}")
}

android {
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
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