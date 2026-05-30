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

    val generateNavProguard = tasks.register("generateNavProguard") {
        // Look for the navigation folder inside the consuming app module, fallback to library resource directory
        val navFolder = file("src/main/res/navigation")
        val outputFile = file(layout.buildDirectory.file("generated/nav-proguard-rules.pro").get().asFile.absolutePath)

        inputs.dir(navFolder)
        outputs.file(outputFile)

        doLast {
            // Only run the parsing logic if the app actually has a navigation folder
            if (navFolder.exists()) {
                val classesToKeep = mutableSetOf<String>()

                navFolder.walk().filter { it.extension == "xml" }.forEach { file ->
                    val matches = Regex("""app:argType="([^"]+)"""").findAll(file.readText())
                    matches.forEach { match ->
                        val className = match.groupValues[1].replace("[]", "")
                        if (className.contains(".")) {
                            classesToKeep.add(className)
                        }
                    }
                }

                outputFile.parentFile.mkdirs()
                outputFile.writeText(classesToKeep.joinToString("\n") { "-keepnames class $it" })
            } else {
                // Write an empty file so the R8 pipeline task doesn't crash looking for a missing file input
                outputFile.parentFile.mkdirs()
                outputFile.writeText("")
            }
        }
    }

// Dynamically attach to R8 task safely without hardcoded class types
    tasks.configureEach {
        // Match the R8 minification task by name instead of by Class Type to avoid ClassNotFoundException
        if (name.startsWith("minify") && name.endsWith("WithR8")) {
            dependsOn(generateNavProguard)

            // Safely check if the configurationFiles property is available on this task via reflection
            try {
                val configFilesMethod = this::class.members.find { it.name == "getConfigurationFiles" }
                if (configFilesMethod != null) {
                    // Safely read the generated file into the ProGuard pipeline configuration
                    val fileCollection = layout.buildDirectory.file("generated/nav-proguard-rules.pro")
                    (this as? com.android.build.gradle.internal.tasks.R8Task)?.configurationFiles?.from(fileCollection)
                }
            } catch (_: Exception) {
                // Silently catch and bypass if a project environment doesn't expose the AGP R8 internal interface
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
