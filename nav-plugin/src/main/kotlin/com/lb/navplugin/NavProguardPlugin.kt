package com.lb.navplugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.util.Locale

abstract class GenerateNavProguardTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    abstract val navFolder: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val folder = navFolder.orNull?.asFile
        val out = outputFile.get().asFile
        val classesToKeep = mutableSetOf<String>()

        if (folder != null && folder.exists()) {
            folder.walk().filter { it.extension == "xml" }.forEach { file ->
                val matches = Regex("""app:argType="([^"]+)"""").findAll(file.readText())
                matches.forEach { match ->
                    val className = match.groupValues[1].replace("[]", "")
                    if (className.contains(".")) {
                        classesToKeep.add(className)
                    }
                }
            }
        }

        out.parentFile.mkdirs()
        out.writeText(classesToKeep.joinToString("\n") { "-keep class $it { *; }" })
    }
}

class NavProguardPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.application") {
            val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val variantName = variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                val taskProvider = project.tasks.register("generateNavProguard$variantName", GenerateNavProguardTask::class.java) {
                    val navFolderFile = project.file("src/main/res/navigation")
                    if (navFolderFile.exists()) {
                        navFolder.set(navFolderFile)
                    }
                    outputFile.set(project.layout.buildDirectory.file("generated/nav-proguard-${variant.name}.pro"))
                }

                variant.proguardFiles.add(taskProvider.flatMap { it.outputFile })
            }
        }
    }
}
