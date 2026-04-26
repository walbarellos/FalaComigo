package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SizeTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens

@Composable
fun EmergencyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(SizeTokens.SymbolCardMinSize),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorTokens.Secondary
        ),
        contentPadding = PaddingValues(SpacingTokens.Md)
    ) {
        Text(
            text = "Urgente",
            style = MaterialTheme.typography.titleMedium,
            color = ColorTokens.OnSecondary
        )
    }
}