package br.com.falacomigo.core.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics

object SwitchAccessGroups {
    fun Modifier.asBoardGroup(): Modifier = this.semantics {
        isTraversalGroup = true
    }

    fun Modifier.asNavigationGroup(): Modifier = this.semantics {
        isTraversalGroup = true
    }

    fun Modifier.asSettingsGroup(): Modifier = this.semantics {
        isTraversalGroup = true
    }
}