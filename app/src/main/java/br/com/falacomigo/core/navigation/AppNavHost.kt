package br.com.falacomigo.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.falacomigo.feature.communication.BoardSelectorScreen
import br.com.falacomigo.feature.communication.CommunicationScreen
import br.com.falacomigo.feature.communication.EmergencyBoardScreen
import br.com.falacomigo.feature.diagnostics.OfflineReadinessScreen
import br.com.falacomigo.feature.diagnostics.TtsHealthScreen
import br.com.falacomigo.feature.editor.BoardEditorScreen
import br.com.falacomigo.feature.editor.BoardEditorViewModel
import br.com.falacomigo.feature.editor.MoveSymbolScreen
import br.com.falacomigo.feature.editor.PinGateScreen
import br.com.falacomigo.feature.editor.SymbolPickerScreen
import br.com.falacomigo.feature.editor.SlotEditorScreen
import br.com.falacomigo.feature.onboarding.CaregiverIntroScreen
import br.com.falacomigo.feature.settings.AboutLicenseScreen
import br.com.falacomigo.feature.settings.AccessibilitySettingsScreen
import br.com.falacomigo.feature.settings.SettingsScreen
import br.com.falacomigo.feature.settings.VoiceSettingsScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.startDestination
    ) {
        composable(AppRoutes.Communication.route) {
            CommunicationScreen(
                onNavigateToEmergency = { navController.navigate(AppRoutes.EmergencyBoard.route) },
                onNavigateToBoardSelector = { navController.navigate(AppRoutes.BoardSelector.route) },
                onNavigateToSettings = { navController.navigate(AppRoutes.Settings.route) }
            )
        }

        composable(AppRoutes.EmergencyBoard.route) {
            EmergencyBoardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.BoardSelector.route) {
            BoardSelectorScreen(
                onNavigateBack = { navController.popBackStack() },
                onBoardSelected = { boardId ->
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAccessibility = { navController.navigate(AppRoutes.AccessibilitySettings.route) },
                onNavigateToVoice = { navController.navigate(AppRoutes.VoiceSettings.route) },
                onNavigateToAbout = { navController.navigate(AppRoutes.AboutLicense.route) },
                onNavigateToOffline = { navController.navigate(AppRoutes.OfflineReadiness.route) },
                onNavigateToEditor = { navController.navigate(AppRoutes.EditorGate.route) }
            )
        }

        composable(AppRoutes.AccessibilitySettings.route) {
            AccessibilitySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.VoiceSettings.route) {
            VoiceSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTtsHealth = { navController.navigate(AppRoutes.TtsHealth.route) }
            )
        }

        composable(AppRoutes.AboutLicense.route) {
            AboutLicenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.OfflineReadiness.route) {
            OfflineReadinessScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.TtsHealth.route) {
            TtsHealthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.EditorGate.route) {
            PinGateScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditorVerified = { boardId ->
                    navController.navigate(AppRoutes.BoardEditor.createRoute(boardId))
                }
            )
        }

        composable(
            route = AppRoutes.BoardEditor.route,
            arguments = listOf(navArgument("boardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString("boardId") ?: return@composable
            BoardEditorScreen(
                boardId = boardId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSymbolPicker = { navController.navigate(AppRoutes.SymbolPicker.createRoute(boardId)) },
                onNavigateToSlotEditor = { symbolId ->
                    navController.navigate(AppRoutes.SlotEditor.createRoute(boardId, symbolId))
                },
                onNavigateToMoveSymbol = { navController.navigate(AppRoutes.MoveSymbol.createRoute(boardId)) }
            )
        }

        composable(
            route = AppRoutes.SymbolPicker.route,
            arguments = listOf(navArgument("boardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString("boardId") ?: return@composable
            
            // Usamos o backStackEntry anterior para obter o mesmo ViewModel da prancha em edição
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AppRoutes.BoardEditor.route)
            }
            val boardEditorViewModel: BoardEditorViewModel = hiltViewModel(parentEntry)

            SymbolPickerScreen(
                boardId = boardId,
                onNavigateBack = { navController.popBackStack() },
                onSymbolSelected = { symbolId ->
                    // Busca o símbolo completo (ou apenas o ID) e adiciona à prancha
                    boardEditorViewModel.addSymbolById(boardId, symbolId)
                    navController.popBackStack()
                },
                onSymbolCreated = { newSymbol ->
                    boardEditorViewModel.addSymbol(boardId, newSymbol)
                }
            )
        }

        composable(
            route = AppRoutes.SlotEditor.route,
            arguments = listOf(
                navArgument("boardId") { type = NavType.StringType },
                navArgument("symbolId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString("boardId") ?: return@composable
            val symbolId = backStackEntry.arguments?.getString("symbolId") ?: return@composable
            SlotEditorScreen(
                boardId = boardId,
                symbolId = symbolId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.MoveSymbol.route,
            arguments = listOf(navArgument("boardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString("boardId") ?: return@composable
            MoveSymbolScreen(
                boardId = boardId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.CaregiverIntro.route) {
            CaregiverIntroScreen(
                onComplete = { navController.popBackStack() }
            )
        }
    }
}