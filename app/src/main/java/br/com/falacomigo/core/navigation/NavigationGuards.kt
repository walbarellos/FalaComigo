package br.com.falacomigo.core.navigation

object NavigationGuards {
    val protectedRoutes = setOf(
        AppRoutes.BoardEditor.route,
        AppRoutes.SymbolPicker.route,
        AppRoutes.SlotEditor.route,
        AppRoutes.MoveSymbol.route
    )

    val publicRoutes = setOf(
        AppRoutes.Communication.route,
        AppRoutes.EmergencyBoard.route,
        AppRoutes.BoardSelector.route,
        AppRoutes.Settings.route,
        AppRoutes.AccessibilitySettings.route,
        AppRoutes.VoiceSettings.route,
        AppRoutes.AboutLicense.route,
        AppRoutes.OfflineReadiness.route,
        AppRoutes.TtsHealth.route,
        AppRoutes.CaregiverIntro.route
    )

    fun isProtected(route: String): Boolean {
        return protectedRoutes.any { route.startsWith(it.substringBefore("/{")) }
    }

    fun requiresGate(route: String): Boolean {
        return route.contains("editor") || route.contains("Editor")
    }
}