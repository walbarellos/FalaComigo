package br.com.falacomigo.core.navigation

sealed class AppRoutes(val route: String) {
    data object Communication : AppRoutes("communication")
    data object BoardSelector : AppRoutes("board_selector")
    data object EmergencyBoard : AppRoutes("emergency_board")
    data object EditorGate : AppRoutes("editor_gate")
    data object BoardEditor : AppRoutes("board_editor/{boardId}") {
        fun createRoute(boardId: String) = "board_editor/$boardId"
    }
    data object SymbolPicker : AppRoutes("symbol_picker/{boardId}") {
        fun createRoute(boardId: String) = "symbol_picker/$boardId"
    }
    data object SlotEditor : AppRoutes("slot_editor/{boardId}/{symbolId}") {
        fun createRoute(boardId: String, symbolId: String) = "slot_editor/$boardId/$symbolId"
    }
    data object MoveSymbol : AppRoutes("move_symbol/{boardId}") {
        fun createRoute(boardId: String) = "move_symbol/$boardId"
    }
    data object Settings : AppRoutes("settings")
    data object AccessibilitySettings : AppRoutes("settings/accessibility")
    data object VoiceSettings : AppRoutes("settings/voice")
    data object AboutLicense : AppRoutes("settings/about")
    data object OfflineReadiness : AppRoutes("settings/offline")
    data object TtsHealth : AppRoutes("settings/tts_health")
    data object CaregiverIntro : AppRoutes("onboarding")

    companion object {
        val startDestination = Communication.route
    }
}