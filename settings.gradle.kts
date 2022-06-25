pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }

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
            version("ksp", "1.6.21-1.0.5")
            version("kotlin", "1.6.21")
            version("agp", "7.0.1")

            library("ksp-api", "com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")

            plugin("ksp", "com.google.devtools.ksp").versionRef("ksp")
            plugin("androidlib", "com.android.library").versionRef("agp")
            plugin("kmp", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
        }
        create("androidConfig") {
            version("compileSdk", "31")
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kuri"

include(":api")
include(":processor")
include(":jvmconsumer")
include(":kmptestlib")