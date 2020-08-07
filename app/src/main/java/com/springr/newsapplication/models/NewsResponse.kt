package com.springr.newsapplication.models


import com.springr.newsapplication.models.Article

data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)