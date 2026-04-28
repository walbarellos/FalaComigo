package br.com.falacomigo.core.seed

import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.BoardUiModel

object SeedSymbols {
    private const val ARASAAC_BASE_URL = "https://static.arasaac.org/pictograms/"
    private const val CACHE_KEY = "?v=20240426_V33_STABLE" 

    val symbols: List<SymbolUiModel> = listOf(
        // --- CATEGORIA: BÁSICOS ORIGINAIS DO DIFF (Com Imagens Locais) ---
        SymbolUiModel(id = "eu", label = "Eu", spokenText = "Eu", categoryId = "basic", imagePath = "sym_eu"),
        SymbolUiModel(id = "voce", label = "Você", spokenText = "Você", categoryId = "basic", imagePath = "sym_voce"),
        SymbolUiModel(id = "feliz", label = "Feliz", spokenText = "Estou feliz", categoryId = "emocoes", imagePath = "sym_feliz"),
        SymbolUiModel(id = "triste", label = "Triste", spokenText = "Estou triste", categoryId = "emocoes", imagePath = "sym_triste"),
        SymbolUiModel(id = "bravo", label = "Bravo", spokenText = "Estou bravo", categoryId = "emocoes", imagePath = "sym_bravo"),
        SymbolUiModel(id = "com_medo", label = "Medo", spokenText = "Estou com medo", categoryId = "emocoes", imagePath = "sym_com_medo"),
        SymbolUiModel(id = "cansado", label = "Cansado", spokenText = "Estou cansado", categoryId = "necessidades", imagePath = "sym_cansado"),
        SymbolUiModel(id = "frustrado", label = "Frustrado", spokenText = "Estou frustrado", categoryId = "emocoes", imagePath = "sym_frustrado"),
        SymbolUiModel(id = "com_fome", label = "Com Fome", spokenText = "Estou com fome", categoryId = "necessidades", imagePath = "sym_com_fome"),
        SymbolUiModel(id = "com_sede", label = "Com Sede", spokenText = "Estou com sede", categoryId = "necessidades", imagePath = "sym_com_sede"),
        SymbolUiModel(id = "dor", label = "Dor", spokenText = "Estou com dor", categoryId = "saude", isEmergency = true, imagePath = "sym_dor"),
        SymbolUiModel(id = "machucado", label = "Machucado", spokenText = "Estou machucado", categoryId = "saude", isEmergency = true, imagePath = "sym_machucado"),
        SymbolUiModel(id = "banheiro", label = "Banheiro", spokenText = "Preciso ir ao banheiro", categoryId = "necessidades", imagePath = "sym_banheiro"),
        SymbolUiModel(id = "agua", label = "Água", spokenText = "Quero água", categoryId = "necessidades", imagePath = "sym_agua"),
        SymbolUiModel(id = "ajuda", label = "Ajuda", spokenText = "Preciso de ajuda", categoryId = "emergencia", isEmergency = true, imagePath = "sym_ajuda"),
        SymbolUiModel(id = "quero_parar", label = "Quero Parar", spokenText = "Quero parar", categoryId = "basic", imagePath = "sym_quero_parar"),

        // --- CATEGORIA: URGENTE / EMERGÊNCIA (NOVAS PROTEÇÕES) ---
        SymbolUiModel(id = "bater", label = "Bater", spokenText = "Ele me bateu", categoryId = "emergencia", isEmergency = true, imageUrl = "${ARASAAC_BASE_URL}4714/4714_500.png$CACHE_KEY"),
        SymbolUiModel(id = "roubo", label = "Roubo", spokenText = "Fui roubado", categoryId = "emergencia", isEmergency = true, imageUrl = "${ARASAAC_BASE_URL}8210/8210_500.png$CACHE_KEY"),
        SymbolUiModel(id = "denunciar", label = "Denunciar", spokenText = "Quero denunciar", categoryId = "emergencia", isEmergency = true, imageUrl = "${ARASAAC_BASE_URL}14006/14006_500.png$CACHE_KEY"),

        // --- CATEGORIA: SOCIAL (IDs VERIFICADOS) ---
        SymbolUiModel(id = "oi", label = "Oi", spokenText = "Olá, tudo bem?", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}6522/6522_500.png$CACHE_KEY"),
        SymbolUiModel(id = "tchau", label = "Tchau", spokenText = "Tchau, até logo", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}6028/6028_500.png$CACHE_KEY"),
        SymbolUiModel(id = "sim", label = "Sim", spokenText = "Sim", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}5584/5584_500.png$CACHE_KEY"),
        SymbolUiModel(id = "nao", label = "Não", spokenText = "Não", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}5526/5526_500.png$CACHE_KEY"),
        SymbolUiModel(id = "por_favor", label = "Por favor", spokenText = "Por favor", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}8195/8195_500.png$CACHE_KEY"),
        SymbolUiModel(id = "obrigado", label = "Obrigado", spokenText = "Obrigado", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}38783/38783_500.png$CACHE_KEY"),
        SymbolUiModel(id = "desculpe", label = "Desculpe", spokenText = "Me desculpe", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}11625/11625_500.png$CACHE_KEY"),
        SymbolUiModel(id = "nao_entendi", label = "Não Entendi", spokenText = "Não nos entendemos", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}11315/11315_500.png$CACHE_KEY"),
        SymbolUiModel(id = "bom_dia", label = "Bom dia", spokenText = "Bom dia", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}38568/38568_500.png$CACHE_KEY"),
        SymbolUiModel(id = "boa_noite", label = "Boa noite", spokenText = "Boa noite", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}38569/38569_500.png$CACHE_KEY"),
        SymbolUiModel(id = "te_amo_papai", label = "Amo Papai", spokenText = "Te amo papai", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}37799/37799_500.png$CACHE_KEY"),
        SymbolUiModel(id = "te_amo_mamae", label = "Amo Mamãe", spokenText = "Te amo mamãe", categoryId = "social", imageUrl = "${ARASAAC_BASE_URL}37799/37799_500.png$CACHE_KEY"),

        // --- CATEGORIA: SENSORIAL ---
        SymbolUiModel(id = "muito_barulho", label = "Muito Barulho", spokenText = "Está muito barulho aqui", categoryId = "sensorial", imageUrl = "${ARASAAC_BASE_URL}2663/2663_500.png$CACHE_KEY"),
        SymbolUiModel(id = "quero_silencio", label = "Silêncio", spokenText = "Quero silêncio, por favor", categoryId = "sensorial", imageUrl = "${ARASAAC_BASE_URL}5936/5936_500.png$CACHE_KEY"),
        
        // --- CATEGORIA: ALIMENTAÇÃO ---
        SymbolUiModel(id = "comer", label = "Comer", spokenText = "Quero comer", categoryId = "alimentacao", imageUrl = "${ARASAAC_BASE_URL}38413/38413_500.png$CACHE_KEY"),
        SymbolUiModel(id = "suco", label = "Suco", spokenText = "Quero suco", categoryId = "alimentacao", imageUrl = "${ARASAAC_BASE_URL}4732/4732_500.png$CACHE_KEY"),
        SymbolUiModel(id = "fruta", label = "Fruta", spokenText = "Quero fruta", categoryId = "alimentacao", imageUrl = "${ARASAAC_BASE_URL}39824/39824_500.png$CACHE_KEY"),
        SymbolUiModel(id = "pao", label = "Pão", spokenText = "Quero pão", categoryId = "alimentacao", imageUrl = "${ARASAAC_BASE_URL}3122/3122_500.png$CACHE_KEY"),

        // --- CATEGORIA: ATIVIDADES / LUGARES ---
        SymbolUiModel(id = "brincar", label = "Brincar", spokenText = "Quero brincar", categoryId = "atividades", imageUrl = "${ARASAAC_BASE_URL}2439/2439_500.png$CACHE_KEY"),
        SymbolUiModel(id = "escola", label = "Escola", spokenText = "Vamos para a escola", categoryId = "lugares", imageUrl = "${ARASAAC_BASE_URL}3082/3082_500.png$CACHE_KEY"),
        SymbolUiModel(id = "casa", label = "Casa", spokenText = "Quero ir para casa", categoryId = "lugares", imageUrl = "${ARASAAC_BASE_URL}2317/2317_500.png$CACHE_KEY"),
        SymbolUiModel(id = "parque", label = "Parque", spokenText = "Quero ir ao parque", categoryId = "lugares", imageUrl = "${ARASAAC_BASE_URL}32987/32987_500.png$CACHE_KEY"),
        SymbolUiModel(id = "quarto", label = "Quarto", spokenText = "Ir para o quarto", categoryId = "lugares", imageUrl = "${ARASAAC_BASE_URL}33068/33068_500.png$CACHE_KEY"),
        SymbolUiModel(id = "bola", label = "Bola", spokenText = "Quero jogar bola", categoryId = "atividades", imageUrl = "${ARASAAC_BASE_URL}2269/2269_500.png$CACHE_KEY"),
        SymbolUiModel(id = "tablet", label = "Tablet", spokenText = "Quero o tablet", categoryId = "atividades", imageUrl = "${ARASAAC_BASE_URL}28099/28099_500.png$CACHE_KEY"),
        SymbolUiModel(id = "desenho", label = "Desenho", spokenText = "Quero ver desenho", categoryId = "atividades", imageUrl = "${ARASAAC_BASE_URL}8089/8089_500.png$CACHE_KEY"),
        SymbolUiModel(id = "xadrez", label = "Xadrez", spokenText = "Quero jogar xadrez", categoryId = "atividades", imageUrl = "${ARASAAC_BASE_URL}3054/3054_500.png$CACHE_KEY"),

        // --- CATEGORIA: AÇÕES ---
        SymbolUiModel(id = "quero", label = "Quero", spokenText = "Eu quero", categoryId = "acoes", imageUrl = "${ARASAAC_BASE_URL}5441/5441_500.png$CACHE_KEY"),
        SymbolUiModel(id = "nao_quero", label = "Não Quero", spokenText = "Eu não quero", categoryId = "acoes", imageUrl = "${ARASAAC_BASE_URL}6156/6156_500.png$CACHE_KEY"),
        SymbolUiModel(id = "ir", label = "Ir", spokenText = "Eu quero ir", categoryId = "acoes", imageUrl = "${ARASAAC_BASE_URL}8142/8142_500.png$CACHE_KEY"),
        SymbolUiModel(id = "olhar", label = "Ver", spokenText = "Eu quero ver", categoryId = "acoes", imageUrl = "${ARASAAC_BASE_URL}6564/6564_500.png$CACHE_KEY"),

        // --- CATEGORIA: NECESSIDADES ADICIONAIS ---
        SymbolUiModel(id = "calor", label = "Calor", spokenText = "Estou com calor", categoryId = "necessidades", imageUrl = "${ARASAAC_BASE_URL}2300/2300_500.png$CACHE_KEY"),
        SymbolUiModel(id = "frio", label = "Frio", spokenText = "Estou com frio", categoryId = "necessidades", imageUrl = "${ARASAAC_BASE_URL}35557/35557_500.png$CACHE_KEY"),
        SymbolUiModel(id = "dormir", label = "Dormir", spokenText = "Quero dormir", categoryId = "necessidades", imageUrl = "${ARASAAC_BASE_URL}6479/6479_500.png$CACHE_KEY"),
        SymbolUiModel(id = "remedio", label = "Remédio", spokenText = "Preciso de remédio", categoryId = "necessidades", imageUrl = "${ARASAAC_BASE_URL}30117/30117_500.png$CACHE_KEY"),
        SymbolUiModel(id = "escovar", label = "Escovar", spokenText = "Escovar os dentes", categoryId = "necessidades", imageUrl = "${ARASAAC_BASE_URL}6971/6971_500.png$CACHE_KEY"),

        // --- CATEGORIA: NUMERAIS E QUANTIDADE ---
        SymbolUiModel(id = "uma_hora", label = "1 Hora", spokenText = "Uma hora", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}29452/29452_500.png$CACHE_KEY"),
        SymbolUiModel(id = "mais", label = "Mais", spokenText = "Mais", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}3220/3220_500.png$CACHE_KEY"),
        SymbolUiModel(id = "acabou", label = "Acabou", spokenText = "Acabou", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}28429/28429_500.png$CACHE_KEY"),
        SymbolUiModel(id = "pouco", label = "Pouco", spokenText = "Só um pouco", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}7209/7209_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_0", label = "0", spokenText = "Zero", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2626/2626_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_1", label = "1", spokenText = "Um", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2627/2627_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_2", label = "2", spokenText = "Dois", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2628/2628_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_3", label = "3", spokenText = "Três", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2629/2629_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_4", label = "4", spokenText = "Quatro", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2630/2630_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_5", label = "5", spokenText = "Cinco", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2631/2631_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_6", label = "6", spokenText = "Seis", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2632/2632_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_7", label = "7", spokenText = "Sete", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2633/2633_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_8", label = "8", spokenText = "Oito", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2634/2634_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_9", label = "9", spokenText = "Nove", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}2635/2635_500.png$CACHE_KEY"),
        SymbolUiModel(id = "num_10", label = "10", spokenText = "Dez", categoryId = "numeral", imageUrl = "${ARASAAC_BASE_URL}29254/29254_500.png$CACHE_KEY")
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
            symbols = SeedSymbols.symbols.filter { it.category.id in listOf("necessidades", "basic", "saude") }
        ),
        BoardUiModel(
            id = "social",
            title = "Social",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category.id == "social" }
        ),
        BoardUiModel(
            id = "sensorial",
            title = "Sentir",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category.id == "sensorial" || it.category.id == "emocoes" }
        ),
        BoardUiModel(
            id = "atividades",
            title = "Lugar/Ação",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category.id in listOf("atividades", "lugares", "acoes") }
        ),
        BoardUiModel(
            id = "alimentacao",
            title = "Comer",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category.id == "alimentacao" }
        ),
        BoardUiModel(
            id = "numeral",
            title = "Números",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.category.id == "numeral" }
        ),
        BoardUiModel(
            id = "urgente",
            title = "Urgente",
            columns = 4,
            symbols = SeedSymbols.symbols.filter { it.isEmergency || it.category.id == "emergencia" || it.category.id == "saude" },
            isEmergency = true
        )
    )

    fun findById(id: String): BoardUiModel? = boards.find { it.id == id }
    val defaultBoard get() = boards.first()
}