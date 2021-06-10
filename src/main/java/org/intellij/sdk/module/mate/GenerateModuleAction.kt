package org.intellij.sdk.module.mate

import org.intellij.sdk.module.mate.config.InvalidConfigException
import org.intellij.sdk.module.mate.config.ModuleTemplateReader
import org.intellij.sdk.module.mate.config.ModuleTemplateWriter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.intellij.sdk.module.mate.config.Config

class GenerateModuleAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return

        val moduleBaseName = askUserForModuleBaseName(project) ?: return

        if (moduleBaseName.isBlank()) {
            Messages.showMessageDialog(project, "You have to input module's base name", "No name specified", null)
            return
        }

        try {
            val moduleTemplates = ModuleTemplateReader.readModuleTemplates(basePath, moduleBaseName)
            WriteCommandAction.runWriteCommandAction(project) {
                ModuleTemplateWriter.writeModuleTemplates(moduleTemplates, moduleBaseName, basePath)
            }
        } catch (e: InvalidConfigException) {
            Messages.showErrorDialog(project, e.message, "Invalid config")
        }
    }

    private fun askUserForModuleBaseName(project: Project): String? {
        val inputName: String? = Messages.showInputDialog(project, "Enter new module base name", "New Module", null)
        return inputName?.trim()
    }
}