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

        writeConfigDirectories(config.directories, projectDir)
        writeStaticFiles(template, projectDir)
        writeTemplateFiles(template, projectDir, moduleConfig)
        val gradleSettings = writeGradleSettings(config.name, projectRoot, genDir)
        val filesToRefresh = mutableListOf(projectDir)
        gradleSettings?.let { filesToRefresh.add(it) }
        LocalFileSystem.getInstance().refreshIoFiles(filesToRefresh)
    }

    private fun writeConfigDirectories(paths: List<String>, root: File) {
        paths.map { root.resolve(it) }
            .forEach { Files.createDirectories(it.toPath()) }
    }

//    private fun writeTemplateDirectories(template: ModuleTemplate, moduleDirectory: File) {
//        template.templateResources.fileMaps.filterNot { it.isFile }.forEach { fileMap ->
//            val target = getRelativeToModule(fileMap.dest, template.staticResources, moduleDirectory)
//            Files.createDirectories(target.toPath())
//        }
//    }

    private fun writeStaticFiles(template: ModuleTemplate, moduleDirectory: File) {
        template.staticResources.fileMaps.forEach { fileMap ->
            val target = getRelativeToModule(fileMap.dest, template.staticResources, moduleDirectory)
            fileMap.source.copyTo(target, overwrite = true)
        }
    }

    private fun writeTemplateFiles(template: ModuleTemplate, moduleDirectory: File, config: ModuleConfig) {
        val templateProperties = mapOf(Settings.moduleTemplateKey to config)
        val templateResources = template.templateResources
        templateResources.fileMaps
            .forEach { templateFile ->
                val targetFile = getRelativeToModule(templateFile.dest, template.templateResources, moduleDirectory)
                targetFile.parentFile.mkdirs()
                targetFile.createNewFile()
                TemplateResolver.resolveTemplate(templateFile.source, targetFile, templateProperties)
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
