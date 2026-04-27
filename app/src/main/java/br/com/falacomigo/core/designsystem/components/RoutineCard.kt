package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SizeTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.RoutineUiModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun RoutineCard(
    routine: RoutineUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (routine.isEmergency) ColorTokens.SecondaryContainer else ColorTokens.Surface
    val contentDesc = "${routine.title}, ${routine.subtitle}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.contentDescription = contentDesc
                this.role = Role.Button
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, ColorTokens.OutlineVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IMAGEM DA ROTINA (Container à esquerda)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorTokens.PrimaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                val model = if (!routine.imageUrl.isNullOrEmpty()) routine.imageUrl else routine.imagePath
                
                if (model != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(model)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ColorTokens.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorTokens.OnSurface
                )
                if (routine.subtitle.isNotEmpty()) {
                    Text(
                        text = routine.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTokens.OnSurfaceVariant
                    )
                }
            }
        }
    }
}