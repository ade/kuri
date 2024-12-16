includeBuild("buildPlugin")

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
        create("androidConfig") {
            version("compileSdk", "34")
            version("minSdk", "21")
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