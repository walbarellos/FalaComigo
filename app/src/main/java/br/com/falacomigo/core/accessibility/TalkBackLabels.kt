package br.com.falacomigo.core.accessibility

object TalkBackLabels {
    fun symbolLabel(label: String): String = "$label, símbolo de comunicação"

    fun buttonUrgente(): String = "Abrir comunicação urgente"

    fun speakPhrase(): String = "Falar frase"

    fun clearPhrase(): String = "Limpar frase"

    fun editBoard(): String = "Editar prancha"

    fun openSettings(): String = "Abrir configurações"

    fun back(): String = "Voltar"

    fun goToEmergency(): String = "Ir para urgência"

    fun routineSelected(title: String, subtitle: String): String = "$title, $subtitle"

    fun settingsAccessibility(): String = "Configurações de acessibilidade"

    fun settingsVoice(): String = "Configurações de voz"

    fun settingsAbout(): String = "Sobre e licenças"

    fun testVoice(): String = "Testar voz"
}