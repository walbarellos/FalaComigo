package br.com.falacomigo.core.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

object AccessibleActions {
    fun Modifier.asAccessibleButton(): Modifier = this.semantics { role = Role.Button }

    fun Modifier.asAccessibleContainer(): Modifier = this.semantics { }
}