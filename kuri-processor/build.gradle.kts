plugins {
    kotlin("jvm")
    id("maven-publish")
    id("convention.publication")
}

project.extensions.getByType<PublishingExtension>().apply {
    publications {
        register<MavenPublication>("release") {
            groupId = project.properties["groupId"]!!.toString()
            artifactId = "kuri-processor"
            version = project.properties["version"]!!.toString()

            from(components["java"])
        }
    }
}

dependencies {
    implementation(project(":kuri-api"))

    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.kotest:kotest-assertions-core:5.1.0")
}