package br.com.falacomigo.core.seed

import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.BoardUiModel

object SeedSymbols {
    val symbols: List<SymbolUiModel> = listOf(
        SymbolUiModel(id = "eu", label = "Eu", spokenText = "Eu", category = "basic"),
        SymbolUiModel(id = "feliz", label = "Feliz", spokenText = "Estou feliz", category = "emocoes"),
        SymbolUiModel(id = "com_medo", label = "Com medo", spokenText = "Estou com medo", category = "emocoes"),
        SymbolUiModel(id = "cansado", label = "Cansado", spokenText = "Estou cansado", category = "necessidades"),
        SymbolUiModel(id = "voce", label = "Você", spokenText = "Você", category = "basic"),
        SymbolUiModel(id = "triste", label = "Triste", spokenText = "Estou triste", category = "emocoes"),
        SymbolUiModel(id = "frustrado", label = "Frustrado", spokenText = "Estou frustrado", category = "emocoes"),
        SymbolUiModel(id = "com_fome", label = "Com Fome", spokenText = "Estou com fome", category = "necessidades"),
        SymbolUiModel(id = "dor", label = "Dor", spokenText = "Estou com dor", category = "saude", isEmergency = true),
        SymbolUiModel(id = "bravo", label = "Bravo", spokenText = "Estou bravo", category = "emocoes"),
        SymbolUiModel(id = "machucado", label = "Machucado", spokenText = "Estou machucado", category = "saude", isEmergency = true),
        SymbolUiModel(id = "com_sede", label = "Com Sede", spokenText = "Estou com sede", category = "necessidades"),
        SymbolUiModel(id = "banheiro", label = "Banheiro", spokenText = "Preciso ir ao banheiro", category = "necessidades"),
        SymbolUiModel(id = "agua", label = "Agua", spokenText = "Quero água", category = "necessidades"),
        SymbolUiModel(id = "ajuda", label = "Ajuda", spokenText = "Preciso de ajuda", category = "emergencia", isEmergency = true),
        SymbolUiModel(id = "quero_parar", label = "Quero Parar", spokenText = "Quero parar", category = "basic")
    )

    fun findById(id: String): SymbolUiModel? = symbols.find { it.id == id }
}

object SeedBoards {
    val boards: List<BoardUiModel> = listOf(
        BoardUiModel(
            id = "comunicacao",
            title = "Comunicação",
            columns = 4,
            symbols = SeedSymbols.symbols
        ),
        BoardUiModel(
            id = "urgente",
            title = "Urgente",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.isEmergency || it.category == "emergencia" || it.category == "saude" }
        ),
        BoardUiModel(
            id = "emocoes",
            title = "Emoções",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category == "emocoes" }
        ),
        BoardUiModel(
            id = "necessidades",
            title = "Necessidades",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category == "necessidades" }
        )
    )

    fun findById(id: String): BoardUiModel? = boards.find { it.id == id }
    val defaultBoard get() = boards.first()
}