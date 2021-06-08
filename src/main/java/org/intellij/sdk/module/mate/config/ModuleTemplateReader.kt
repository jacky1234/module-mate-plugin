package org.intellij.sdk.module.mate.config

import org.intellij.sdk.module.mate.model.ModuleTemplate
import org.intellij.sdk.module.mate.model.Resources
import java.io.File
import java.io.FileFilter

object ModuleTemplateReader {

    fun readModuleTemplates(projectPath: String): List<ModuleTemplate> {
        val templatesPath = File(projectPath)
            .resolve(Settings.templateFolderName)
        if (!templatesPath.exists()) {
            throw InvalidConfigException("${Settings.templateFolderName} not found in the project")
        }
        return templatesPath
            .listFiles(FileFilter { it.isDirectory })
            .map { readModule(it) }
    }

    fun readResolvedConfig(config: File, props: Map<String, Any>): Config {
        val resolvedTemplateString = TemplateResolver.resolveTemplate(config, props)
        return Config.loadAs(resolvedTemplateString)
    }

    private fun readModule(moduleDirectory: File): ModuleTemplate {
        val configFile = moduleDirectory.resolve(Settings.configFileName)
        val staticResources = getResources(moduleDirectory, Settings.staticFilesName)
        val templateResources = getResources(moduleDirectory, Settings.templateFilesName)
        return ModuleTemplate(configFile, staticResources, templateResources)
    }

    private fun getResources(moduleDirectory: File, resourcesName: String): Resources {
        val resourcesDirectory = moduleDirectory.resolve(resourcesName)
        return Resources(findFiles(resourcesDirectory), resourcesDirectory)
    }

    private fun findFiles(directory: File): List<File> {
        return directory.walk().toList().filterNot { it.isDirectory }
    }
}