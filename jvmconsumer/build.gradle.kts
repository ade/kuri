plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
    application
}

application {
    mainClass.set("se.ade.kuri.client.MainKt")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

ksp {
    arg("se.ade.kuri.placeholder.start", "{")
    arg("se.ade.kuri.placeholder.end", "}")
}

dependencies {
    implementation(project(":kuri-api"))
    implementation(project(":kmptestlib"))
    ksp(project(":kuri-processor"))

    implementation(kotlin("test"))
    implementation(libs.kotest.assertions.core)
}
