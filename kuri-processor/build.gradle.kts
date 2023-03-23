plugins {
    kotlin("jvm")
    id("maven-publish")
    id("convention.publication")
}

java {
    withJavadocJar()

    //Creates sourcesJar task
    withSourcesJar()
}

project.extensions.getByType<PublishingExtension>().apply {
    publications {
        create<MavenPublication>("kuri") {
            groupId = project.properties["groupId"]!!.toString()
            from(components["kotlin"])

            //Doesn't get uploaded if we don't specify it explicitly for some reason
            artifact(tasks.getByName("sourcesJar"))
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