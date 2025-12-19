package com.example.app

import com.example.core.DataProcessor
import com.example.utils.StringHelper
import org.apache.commons.text.WordUtils

fun main() {
    val processor = DataProcessor()
    val result = processor.process("hello world")
    println("Processed: $result")

    val joined = StringHelper.joinStrings("Gradle", "Plugin", "Example")
    println("Joined: $joined")

    val capitalized = WordUtils.capitalizeFully("gradle project report plugin")
    println("Capitalized: $capitalized")
}
