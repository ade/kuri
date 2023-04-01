pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }

    //Configure the android library plugin so it resolves
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android.library")) {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("ksp", "1.8.0-1.0.9")
            version("kotlin", "1.8.0")
            version("agp", "7.0.1")
            version("kotlinpoet", "1.12.0")

            library("ksp-api", "com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")
            library("kotlinpoet", "com.squareup", "kotlinpoet").versionRef("kotlinpoet")
            library("kotlinpoet_ksp", "com.squareup", "kotlinpoet-ksp").versionRef("kotlinpoet")

            plugin("ksp", "com.google.devtools.ksp").versionRef("ksp")
            plugin("androidlib", "com.android.library").versionRef("agp")
            plugin("kmp", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
        }
        create("androidConfig") {
            version("compileSdk", "31")
        }
    }

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kuri"

include(":kuri-api")
include(":kuri-processor")
include(":jvmconsumer")
include(":kmptestlib")