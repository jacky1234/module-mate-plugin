package org.intellij.sdk.module.mate.config

import org.intellij.sdk.module.mate.ModuleConfig
import org.intellij.sdk.module.mate.model.ModuleTemplate
import org.intellij.sdk.module.mate.model.Resources
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.nio.file.Files

object ModuleTemplateWriter {

    fun writeModuleTemplates(templates: List<ModuleTemplate>, moduleBaseName: String, basePath: String) {
        templates.forEach { writeModuleTemplate(it, moduleBaseName, basePath) }
    }

    private fun writeModuleTemplate(template: ModuleTemplate, moduleBaseName: String, basePath: String) {
        val moduleConfig = ModuleConfig(moduleBaseName)
        val config = ModuleTemplateReader.readResolvedConfig(
            template.configFile,
            mapOf(Settings.moduleTemplateKey to moduleConfig)
        )

        val moduleResolvedName = config.name
        val projectRoot = File(basePath)
        val genDir = config.genDir

        val projectDir = if (genDir == null || genDir.trim().isEmpty()) {
            projectRoot.resolve(moduleResolvedName)
        } else {
            projectRoot.resolve(genDir).resolve(moduleResolvedName)
        }

        writeDirectories(config.directories, projectDir)
        writeStaticFiles(template, projectDir)
        writeTemplateFiles(template, projectDir, moduleConfig)
        val gradleSettings = writeGradleSettings(config.name, projectRoot, genDir)
        val filesToRefresh = mutableListOf(projectDir)
        gradleSettings?.let { filesToRefresh.add(it) }
        LocalFileSystem.getInstance().refreshIoFiles(filesToRefresh)
    }

    private fun writeDirectories(paths: List<String>, root: File) {
        paths.map { root.resolve(it) }
            .forEach { Files.createDirectories(it.toPath()) }
    }

    private fun writeStaticFiles(template: ModuleTemplate, moduleDirectory: File) {
        template.staticResources.files.forEach { file ->
            val target = getRelativeToModule(file, template.staticResources, moduleDirectory)
            file.copyTo(target, overwrite = true)
        }
    }

    private fun writeTemplateFiles(template: ModuleTemplate, moduleDirectory: File, config: ModuleConfig) {
        val templateProperties = mapOf(Settings.moduleTemplateKey to config)
        val templateResources = template.templateResources
        templateResources.files
            .filterNot { it.isDirectory }
            .forEach { templateFile ->
                val targetFile = getRelativeToModule(templateFile, template.templateResources, moduleDirectory)
                targetFile.parentFile.mkdirs()
                targetFile.createNewFile()
                TemplateResolver.resolveTemplate(templateFile, targetFile, templateProperties)
            }
    }

    private fun writeGradleSettings(moduleName: String, projectRoot: File, genDir: String?): File? {
        val gradleFile = projectRoot.resolve(Settings.gradleSettings)
        val gradleFileKts = projectRoot.resolve(Settings.gradleSettingsKts)
        val file = if (gradleFile.exists()) gradleFile else gradleFileKts;
        if (file.exists()) {
            gradleFile.appendText("\ninclude ':$moduleName'")

            if (genDir != null && genDir.isNotBlank()) {
                gradleFile.appendText("\nproject(':${moduleName}').projectDir = new File(settingsDir, './${genDir}/${moduleName}')")
            }
        }

        return file
    }

    private fun getRelativeToModule(originalFile: File, resources: Resources, moduleDirectory: File): File {
        val fileRelativeToResources = originalFile.relativeTo(resources.baseDirectory)
        return moduleDirectory.resolve(fileRelativeToResources)
    }
}
