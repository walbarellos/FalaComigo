package br.com.falacomigo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import br.com.falacomigo.core.designsystem.theme.FalaComigoTheme
import br.com.falacomigo.core.navigation.AppNavHost
import br.com.falacomigo.data.seed.DatabaseSeeder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Garante que o banco de dados seja populado
        lifecycleScope.launch {
            databaseSeeder.seedIfEmpty()
        }

        enableEdgeToEdge()
        setContent {
            FalaComigoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost()
                }
            }
        }
    }
}