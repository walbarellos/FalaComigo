package br.com.falacomigo.di

import android.content.Context
import br.com.falacomigo.data.local.dao.BoardDao
import br.com.falacomigo.data.local.dao.RoutineDao
import br.com.falacomigo.data.local.dao.SymbolDao
import br.com.falacomigo.data.local.database.FalaComigoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FalaComigoDatabase {
        return FalaComigoDatabase.create(context)
    }

    @Provides
    @Singleton
    fun provideSymbolDao(database: FalaComigoDatabase): SymbolDao {
        return database.symbolDao()
    }

    @Provides
    @Singleton
    fun provideBoardDao(database: FalaComigoDatabase): BoardDao {
        return database.boardDao()
    }

    @Provides
    @Singleton
    fun provideRoutineDao(database: FalaComigoDatabase): RoutineDao {
        return database.routineDao()
    }
}