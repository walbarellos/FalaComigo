package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SizeTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.RoutineUiModel

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
        shape = RoundedCornerShape(SizeTokens.BorderRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, ColorTokens.OutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorTokens.OnSurface
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Xs))
                Text(
                    text = routine.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.OnSurfaceVariant
                )
            }
        }
    }
}