plugins {
    kotlin("jvm")
    id("maven-publish")
    id("convention.publish")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":kuri-api"))

    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.kotest:kotest-assertions-core:5.1.0")
}
