package com.bubblelevel.pocket.domain.model

data class AppSettings(
    val unit: AngleUnit = AngleUnit.DEGREES,
    val sound: Boolean = false,
    val haptics: Boolean = true
)
