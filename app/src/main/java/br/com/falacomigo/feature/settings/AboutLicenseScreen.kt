package br.com.falacomigo.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.falacomigo.BuildConfig
import br.com.falacomigo.core.designsystem.tokens.ColorTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLicenseScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre e Licença", color = ColorTokens.OnSurface, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        },
        containerColor = ColorTokens.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorTokens.Background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ColorTokens.OutlineVariant, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ColorTokens.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("FC", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fala Comigo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorTokens.OnSurface
                        )
                        Text(
                            text = "Versão ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorTokens.OnSurfaceVariant
                        )
                        Text(
                            text = "Comunicação Aumentativa e Alternativa com símbolos, voz, favoritos, editor protegido e uso offline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorTokens.OnSurfaceVariant,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            AboutInfoPanel(
                title = "Destaques desta versão",
                body = "• Imagens persistentes para uso offline\n• Prancha inicial com símbolos críticos\n• Grade, foco e categorias\n• Voz configurável em pt-BR\n• Editor protegido por PIN\n• Tela SOS alinhada ao mesmo pipeline visual"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ColorTokens.OutlineVariant, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Desenvolvido por",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorTokens.Primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Willian Albarello",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ColorTokens.OnSurface
                    )
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/walbarellos"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GitHub")
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/walbarellos/FalaComigo"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Repositório")
                    }
                }
            }

            AboutInfoPanel(
                title = "Licença e Créditos",
                body = "Este aplicativo é código aberto e distribuído sob os termos de sua licença original. Os símbolos pictográficos utilizados são de propriedade do Governo de Aragão e foram criados por Sergio Palao para ARASAAC."
            )
        }
    }
}

@Composable
private fun AboutInfoPanel(
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ColorTokens.OutlineVariant, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = ColorTokens.Primary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTokens.OnSurfaceVariant,
                lineHeight = 21.sp
            )
        }
    }
}
