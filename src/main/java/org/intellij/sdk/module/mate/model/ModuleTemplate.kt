package org.intellij.sdk.module.mate.model

import java.io.File

data class ModuleTemplate(
    val configFile: File,
    val staticResources: Resources,
    val templateResources: Resources) {

    fun replaceBaseName(name:String) {
        staticResources.replaceBaseName(name)
        templateResources.replaceBaseName(name)
    }
}