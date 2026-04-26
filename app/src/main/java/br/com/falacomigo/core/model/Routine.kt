package br.com.falacomigo.core.model

data class Routine(
    val id: String,
    val name: String,
    val symbols: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)

data class FavoritePhrase(
    val id: String,
    val text: String,
    val clickCount: Int = 0,
    val lastUsed: Long = System.currentTimeMillis()
)