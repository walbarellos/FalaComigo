package br.com.falacomigo.core.model

/**
 * Define as estratégias de visualização da prancha.
 * Implementa o Strategy Pattern para a UI.
 */
enum class BoardLayoutMode(val id: String, val label: String) {
    GRID("grid", "Grade (Clássico)"),
    PAGER("pager", "Foco (Um por vez)"),
    MMO("mmo", "Densidade (MMO Style)");

    companion object {
        fun fromId(id: String?): BoardLayoutMode = values().find { it.id == id } ?: GRID
    }
}
