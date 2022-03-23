package dev.shellbook.utils

import java.util.Properties

data class ApplicationInfo(
    val version: String
) {

    companion object {

        fun load() : ApplicationInfo {
            return Properties().apply {
                load(ClassLoader.getSystemResourceAsStream("application.properties"))
            }.let {
                ApplicationInfo(it.getProperty("info.version"))
            }
        }
    }
}