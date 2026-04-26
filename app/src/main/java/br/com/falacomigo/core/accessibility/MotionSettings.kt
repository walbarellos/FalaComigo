package br.com.falacomigo.core.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.view.Window

class MotionSettings {
    var reduceMotion: Boolean = false
    var animationsEnabled: Boolean = true

    companion object {
        @Volatile
        private var instance: MotionSettings? = null

        fun getInstance(): MotionSettings {
            return instance ?: synchronized(this) {
                instance ?: MotionSettings().also { instance = it }
            }
        }
    }
}

@Composable
fun rememberMotionSettings(): MotionSettings {
    return remember { MotionSettings.getInstance() }
}