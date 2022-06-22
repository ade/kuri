
buildscript {
    repositories {
        mavenCentral()
        google()
    }

    /* Legacy plugins that are not published on the Gradle Plugin Portal need to be defined as dependencies here.
    * Using the buildscript block to do this is the legacy way and should be avoided when possible. */
    dependencies {
        //Android Gradle Plugin
        classpath("com.android.tools.build:gradle:7.0.1")
    }
}