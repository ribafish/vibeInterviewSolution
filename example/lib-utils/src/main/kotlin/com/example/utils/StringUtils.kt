package com.example.utils

import org.apache.commons.lang3.StringUtils
import com.google.common.base.Joiner

object StringHelper {
    fun capitalize(text: String): String = StringUtils.capitalize(text)

    fun joinStrings(vararg strings: String): String = Joiner.on(", ").join(strings)
}
