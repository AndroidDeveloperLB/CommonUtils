import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.lb.common_utils"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }
    lint {
        targetSdk = 37
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
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    // 1. Define and register the task cleanly using the standard Gradle API
    val generateNavProguard = tasks.register("generateNavProguard") {
        // Dynamically look for the navigation folder inside the module
        val navFolder = file("src/main/res/navigation")
        val outputFile = file(layout.buildDirectory.file("generated/nav-proguard-rules.pro").get().asFile.absolutePath)

        inputs.dir(navFolder).optional(true) // Keeps it safe and ignores if navigation doesn't exist
        outputs.file(outputFile)

        doLast {
            val classesToKeep = mutableSetOf<String>()
            if (navFolder.exists()) {
                navFolder.walk().filter { it.extension == "xml" }.forEach { file ->
                    val matches = Regex("""app:argType="([^"]+)"""").findAll(file.readText())
                    matches.forEach { match ->
                        val className = match.groupValues[1].replace("[]", "")
                        if (className.contains(".")) {
                            classesToKeep.add(className)
                        }
                    }
                }
            }

            outputFile.parentFile.mkdirs()
            // FIX: Use unconditional "-keep class" so R8 doesn't strip reflective array lookups
            outputFile.writeText(classesToKeep.joinToString("\n") { "-keep class $it" })
        }
    }

// 2. Wire the task output into the official Android Components Variant API
    androidComponents {
        onVariants { variant ->
            // Only inject into release builds (or any minified build type)
            if (variant.isMinifyEnabled) {
                // Automatically appends our generated rule file to the ProGuard/R8 configuration pipeline
                variant.proguardFiles.add(generateNavProguard.map { task ->
                    layout.buildDirectory.file("generated/nav-proguard-rules.pro").get()
                })
            }
        }
    }
}

dependencies {
    api("androidx.core:core-ktx:1.18.0")
    api("com.google.android.material:material:1.14.0")
    api("androidx.work:work-runtime-ktx:2.11.2")
    api("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.navigation:navigation-runtime-ktx:2.9.8")
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
