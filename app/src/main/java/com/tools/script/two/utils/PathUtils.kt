package com.tools.script.two.utils

import java.io.File
import com.blankj.utilcode.util.PathUtils as PathUtils2

internal object PathUtils {

    fun getAutoSchemaPath(): String {
        val dir = PathUtils2.getFilesPathExternalFirst()

        File(dir).mkdirs()

        return "$dir/auto_schema.txt"
    }

    fun getEventPath(): String {
        val dir = PathUtils2.getCachePathExternalFirst()

        File(dir).mkdirs()

        return "$dir/event${System.currentTimeMillis()}.txt"
    }
}