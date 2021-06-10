package org.intellij.sdk.module.mate.model

import java.io.File

data class ModuleTemplate(
    val configFile: File,
    val staticResources: Resources,
    val templateResources: Resources)