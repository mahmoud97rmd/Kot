package com.tradingapp.metatrader.domain.models.drawing

data class DrawingObject(
    val id: String,
    val type: DrawingType,
    val points: List<DrawingPoint>,
    val colorHex: String = "#FFFFFF",
    val lineWidth: Float = 2f,
    val locked: Boolean = false
)
