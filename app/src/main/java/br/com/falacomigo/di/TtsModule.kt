package br.com.falacomigo.di

import android.content.Context
import br.com.falacomigo.core.tts.AndroidTtsController
import br.com.falacomigo.core.tts.TtsController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {

    @Provides
    @Singleton
    fun provideTtsController(
        @ApplicationContext context: Context
    ): TtsController {
        return AndroidTtsController(context)
    }
}