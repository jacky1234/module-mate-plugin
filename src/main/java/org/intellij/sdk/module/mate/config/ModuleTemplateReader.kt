package org.intellij.sdk.module.mate.config

import org.intellij.sdk.module.mate.model.FileMap
import org.intellij.sdk.module.mate.model.ModuleTemplate
import org.intellij.sdk.module.mate.model.Resources
import java.io.File
import java.io.FileFilter

object ModuleTemplateReader {

    fun readModuleTemplates(projectPath: String, module: String): List<ModuleTemplate> {
        val templatesPath = File(projectPath)
            .resolve(Settings.templateFolderName)
        if (!templatesPath.exists()) {
            throw InvalidConfigException("${Settings.templateFolderName} not found in the project")
        }
        return templatesPath
            .listFiles(FileFilter { it.isDirectory })
            .map { readModule(it, module) }
    }

    fun readResolvedConfig(config: File, props: Map<String, Any>): Config {
        val resolvedTemplateString = TemplateResolver.resolveTemplate(config, props)
        return Config.loadAs(resolvedTemplateString)
    }

    private fun readModule(moduleDirectory: File, module: String): ModuleTemplate {
        val configFile = moduleDirectory.resolve(Settings.configFileName)
        val staticResources = getResources(moduleDirectory, Settings.staticFilesName, module)
        val templateResources = getResources(moduleDirectory, Settings.templateFilesName, module)
        return ModuleTemplate(configFile, staticResources, templateResources)
    }

    private fun getResources(moduleDirectory: File, resourcesName: String, module: String): Resources {
        val resourcesDirectory = moduleDirectory.resolve(resourcesName)
        return Resources(findFiles(resourcesDirectory, module), resourcesDirectory)
    }

    private fun findFiles(directory: File, module: String): List<FileMap> {
        return directory.walk().toList().filter { it.isFile }.map {
             FileMap(it, File(it.absolutePath.replace("\${module.baseName}", module)))
        }
    }
}