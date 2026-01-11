package com.tradingapp.metatrader.app.core.expert.dsl

class ExpertDslParseException(
    val lineNumber: Int,
    message: String
) : IllegalArgumentException("Line $lineNumber: $message")
