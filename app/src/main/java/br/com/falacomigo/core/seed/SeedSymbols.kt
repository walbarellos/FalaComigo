package br.com.falacomigo.core.seed

import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.BoardUiModel

object SeedSymbols {
    val symbols: List<SymbolUiModel> = listOf(
        // OS 16 ORIGINAIS (Prioridade Máxima - Possuem Imagem)
        SymbolUiModel(id = "eu", label = "Eu", spokenText = "Eu", category = "basic"),
        SymbolUiModel(id = "voce", label = "Você", spokenText = "Você", category = "basic"),
        SymbolUiModel(id = "feliz", label = "Feliz", spokenText = "Estou feliz", category = "emocoes"),
        SymbolUiModel(id = "triste", label = "Triste", spokenText = "Estou triste", category = "emocoes"),
        SymbolUiModel(id = "bravo", label = "Bravo", spokenText = "Estou bravo", category = "emocoes"),
        SymbolUiModel(id = "com_medo", label = "Com medo", spokenText = "Estou com medo", category = "emocoes"),
        SymbolUiModel(id = "cansado", label = "Cansado", spokenText = "Estou cansado", category = "necessidades"),
        SymbolUiModel(id = "frustrado", label = "Frustrado", spokenText = "Estou frustrado", category = "emocoes"),
        SymbolUiModel(id = "com_fome", label = "Com Fome", spokenText = "Estou com fome", category = "necessidades"),
        SymbolUiModel(id = "com_sede", label = "Com Sede", spokenText = "Estou com sede", category = "necessidades"),
        SymbolUiModel(id = "dor", label = "Dor", spokenText = "Estou com dor", category = "saude", isEmergency = true),
        SymbolUiModel(id = "machucado", label = "Machucado", spokenText = "Estou machucado", category = "saude", isEmergency = true),
        SymbolUiModel(id = "banheiro", label = "Banheiro", spokenText = "Preciso ir ao banheiro", category = "necessidades"),
        SymbolUiModel(id = "agua", label = "Água", spokenText = "Quero água", category = "necessidades"),
        SymbolUiModel(id = "ajuda", label = "Ajuda", spokenText = "Preciso de ajuda", category = "emergencia", isEmergency = true),
        SymbolUiModel(id = "quero_parar", label = "Quero Parar", spokenText = "Quero parar", category = "basic"),

        // ADICIONAIS DIFERENTES (Ao final, sem conflito com os originais)
        SymbolUiModel(id = "oi", label = "Oi", spokenText = "Olá, tudo bem?", category = "social"),
        SymbolUiModel(id = "tchau", label = "Tchau", spokenText = "Tchau, até logo", category = "social"),
        SymbolUiModel(id = "sim", label = "Sim", spokenText = "Sim", category = "social"),
        SymbolUiModel(id = "nao", label = "Não", spokenText = "Não", category = "social"),
        SymbolUiModel(id = "por_favor", label = "Por favor", spokenText = "Por favor", category = "social"),
        SymbolUiModel(id = "obrigado", label = "Obrigado", spokenText = "Obrigado", category = "social"),
        SymbolUiModel(id = "muito_barulho", label = "Muito Barulho", spokenText = "Está muito barulho aqui", category = "sensorial"),
        SymbolUiModel(id = "quero_silencio", label = "Quero Silêncio", spokenText = "Quero silêncio, por favor", category = "sensorial"),
        SymbolUiModel(id = "brincar", label = "Brincar", spokenText = "Quero brincar", category = "atividades"),
        SymbolUiModel(id = "escola", label = "Escola", spokenText = "Vamos para a escola", category = "lugares"),
        SymbolUiModel(id = "casa", label = "Casa", spokenText = "Quero ir para casa", category = "lugares")
    )

    fun findById(id: String): SymbolUiModel? = symbols.find { it.id == id }
}

object SeedBoards {
    val boards: List<BoardUiModel> = listOf(
        BoardUiModel(
            id = "comunicacao",
            title = "Todos",
            columns = 4,
            symbols = SeedSymbols.symbols
        ),
        BoardUiModel(
            id = "necessidades",
            title = "Necessidades",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category in listOf("necessidades", "basic", "saude") }
        ),
        BoardUiModel(
            id = "social",
            title = "Social",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category == "social" }
        ),
        BoardUiModel(
            id = "sensorial",
            title = "Sentir",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category == "sensorial" || it.category == "emocoes" }
        ),
        BoardUiModel(
            id = "atividades",
            title = "Lugar/Ação",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category in listOf("atividades", "lugares") }
        ),
        BoardUiModel(
            id = "urgente",
            title = "Urgente",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.isEmergency || it.id == "quero_parar" },
            isEmergency = true
        )
    )

    fun findById(id: String): BoardUiModel? = boards.find { it.id == id }
    val defaultBoard get() = boards.first()
}