package com.lb.navplugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.regex.Pattern

class NavProguardPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val generateNavProguard = project.tasks.register("generateNavProguard") {
            val navFolder = project.file("src/main/res/navigation")
            val outputFile = project.layout.buildDirectory.file("generated/nav-proguard-rules.pro")

            inputs.files(navFolder).optional().withPropertyName("navFolder")
            outputs.file(outputFile).withPropertyName("outputFile")

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

                val out = outputFile.get().asFile
                out.parentFile.mkdirs()
                out.writeText(classesToKeep.joinToString("\n") { "-keep class $it { *; }" })
            }
        }

        project.pluginManager.withPlugin("com.android.application") {
            val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                variant.proguardFiles.add(generateNavProguard.map { 
                    project.layout.buildDirectory.file("generated/nav-proguard-rules.pro").get()
                })
            }
        }
    }
}
