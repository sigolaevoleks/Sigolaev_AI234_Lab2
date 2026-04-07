package com.sigolaev.lab2.model

data class Note(
    val id: Int,
    val title: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorIndex: Int = 0
)
