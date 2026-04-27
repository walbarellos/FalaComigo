package br.com.falacomigo.data.repository

import android.content.Context
import br.com.falacomigo.data.local.dao.CachedPictogramDao
import br.com.falacomigo.data.local.entities.CachedPictogram
import br.com.falacomigo.data.remote.ArasaacApi
import br.com.falacomigo.data.remote.ArasaacPictogram
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArasaacRepository @Inject constructor(
    private val api: ArasaacApi,
    private val dao: CachedPictogramDao,
    @ApplicationContext private val context: Context
) {
    // Busca remota com fallback local
    suspend fun search(query: String): Result<List<ArasaacPictogram>> = runCatching {
        api.bestSearch(text = query).ifEmpty { api.search(text = query) }
    }

    suspend fun resolveId(word: String): Int? {
        return try {
            val results = api.bestSearch(text = word)
            results.firstOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    // Chame isso UMA vez ao instalar o app e salve no Room
    suspend fun seedSymbolsFromApi(words: List<String>) {
        words.forEach { word ->
            if (dao.findByLabel(word) == null) {
                val id = resolveId(word) ?: return@forEach
                val pictogram = ArasaacPictogram(
                    id = id, 
                    keywords = emptyList() // O label virá da palavra buscada no CachedPictogram
                )
                dao.insert(
                    CachedPictogram(
                        id = id,
                        label = word,
                        imageUrl = pictogram.imageUrl
                    )
                )
                // Pré-aquece o cache do Coil
                val request = ImageRequest.Builder(context)
                    .data(pictogram.imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }

    // Salva no Room E dispara download do Coil pro disco
    suspend fun savePictogram(p: ArasaacPictogram) {
        dao.insert(
            CachedPictogram(
                id = p.id,
                label = p.label,
                imageUrl = p.imageUrl
            )
        )
        // Pré-aquece o cache do Coil pra uso offline
        val request = ImageRequest.Builder(context)
            .data(p.imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        context.imageLoader.enqueue(request)
    }

    fun getSaved(): Flow<List<CachedPictogram>> = dao.getAll()
}