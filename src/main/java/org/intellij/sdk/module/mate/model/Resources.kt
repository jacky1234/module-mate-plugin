package org.intellij.sdk.module.mate.model

import java.io.File

data class Resources(val files: List<File>, val baseDirectory: File) {

    fun replaceBaseName(name: String) {
        files.map { File(it.absolutePath.replace("\\{module.baseName\\}", name)) }
    }

}