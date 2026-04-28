package br.com.falacomigo

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp

/**
 * Application principal do FalaComigo.
 *
 * Este ImageLoader é o singleton usado por TODOS os componentes de imagem:
 * - rememberAsyncImagePainter nos SymbolCards
 * - SingletonImageLoader.get(context) no preloader da ViewModel
 *
 * É crítico que sejam o MESMO loader — caso contrário os preloads vão para
 * um cache separado e os cards nunca encontram cache hit.
 *
 * Configurações:
 * - allowHardware(true): texturas direto na GPU, sem cópia de RAM → melhor FPS
 * - memoryCache 25%: guarda ~40-80 pictogramas na RAM (suficiente para 2 boards)
 * - diskCache 256MB: ~2500 pictogramas em 192px armazenados permanentemente
 * - crossfade(false): elimina animação de fade que causa frames perdidos durante scroll
 * - CachePolicy ENABLED em todas as camadas: read + write habilitados
 */
@HiltAndroidApp
class FalaComigoApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .allowHardware(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    // 25% é o sweet spot para grids densos:
                    // guarda o board inteiro sem pressionar o GC
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("arasaac_cache"))
                    // 256MB: ~2500 pictogramas ARASAAC em 192px permanentemente no disco
                    // 512MB era excessivo — aumenta scan time do cache sem ganho real
                    .maxSizeBytes(256L * 1024 * 1024)
                    .build()
            }
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            // crossfade=false: elimina stutter de animação durante scroll rápido
            .crossfade(false)
            .build()
    }
}
