package com.example.core

import com.example.utils.StringHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory

class DataProcessor {
    private val logger = LoggerFactory.getLogger(DataProcessor::class.java)
    private val mapper = ObjectMapper()

    fun process(data: String): String {
        logger.info("Processing data: $data")
        return StringHelper.capitalize(data)
    }
}
