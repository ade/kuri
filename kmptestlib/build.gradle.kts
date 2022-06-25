plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidlib)
}

val groupId = "se.ade.kuri.kmpconsumer"
project.group = groupId

kotlin {
    jvm()
    android()
    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":api"))
            }
        }
    }
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
    add("kspCommonMainMetadata", project(":processor"))
}
