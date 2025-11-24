plugins {
    `maven-publish`
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        //        https://mvnrepository.com/artifact/com.android.tools.build/gradle?repo=google
        classpath("com.android.tools.build:gradle:8.13.1")
        //        https://kotlinlang.org/docs/reference/using-gradle.html https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
