plugins {
    alias(libs.plugins.androidlib) apply false
    alias(libs.plugins.kmp) apply false
    alias(libs.plugins.jvm) apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

tasks {
    register("kuriTest") {
        dependsOn(":kmptestlib:cleanJvmTest")
        dependsOn(":kmptestlib:jvmTest")
    }
    register("kuriPublish") {
        //Will publish snapshot if version ends with -SNAPSHOT (https://vanniktech.github.io/gradle-maven-publish-plugin/central/#publishing-snapshots)
        dependsOn("kuriTest")
        dependsOn(":kuri-api:publishAllPublicationsToMavenCentralRepository")
        dependsOn(":kuri-processor:publishAllPublicationsToMavenCentralRepository")
    }
}