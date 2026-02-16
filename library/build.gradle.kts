import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }
    lint {
        targetSdk = 36
    }

    buildTypes {
        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = false
    }
    namespace = "com.lb.common_utils"

    publishing {
        singleVariant("release")
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // used to have resource annotations (like DrawableRes) to both fields and parameters
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
            }
        }
    }
}

dependencies {
    api("androidx.core:core-ktx:1.17.0")
    api("com.google.android.material:material:1.13.0")
    api("androidx.work:work-runtime-ktx:2.11.1")
    api("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.core:core-ktx:1.17.0")
}
