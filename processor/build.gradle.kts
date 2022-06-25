plugins {
    kotlin("jvm")
}

val kspVersion: String by project

dependencies {
    implementation(project(":api"))

    implementation(libs.ksp.api)

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.kotest:kotest-assertions-core:5.1.0")
}