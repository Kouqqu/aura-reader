package com.aura.reader.data.model

data class FlibustaBook(
    val id: String,
    val title: String,
    val author: String = "",
    val annotation: String = "",
    val coverUrl: String? = null,
    val fb2Url: String? = null,
    val epubUrl: String? = null,
    val downloadSize: String? = null,
    val isCategory: Boolean = false,
    val categoryPath: String? = null
)
