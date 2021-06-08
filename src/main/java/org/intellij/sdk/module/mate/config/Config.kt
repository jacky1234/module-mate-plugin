package org.intellij.sdk.module.mate.config

import org.yaml.snakeyaml.Yaml
import java.io.StringReader

/**
 *  Representation of config.yml file inside of each module template
 */
class Config {
    /**
     * 模块名字
     */
    lateinit var name: String

    /**
     * 需要生成的dirs
     */
    var directories: List<String> = emptyList()

    /**
     * 模块在哪个目录生成
     */
    var genDir: String? = null

    companion object {
        fun loadAs(resolvedTemplateString: String): Config {
            val yaml = Yaml()
            Thread.currentThread().contextClassLoader = Config::class.java.classLoader
            return yaml.loadAs(StringReader(resolvedTemplateString), Config::class.java)
        }
    }
}