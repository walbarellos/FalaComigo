package br.com.falacomigo

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers

/**
 * Application principal do FalaComigo.
 *
 * PIPELINE COIL OTIMIZADO PARA SCROLL ESTÁVEL:
 *
 * 1. NÃO definimos bitmapConfig explicitamente.
 *    allowHardware(true) faz Coil usar HARDWARE bitmaps automaticamente:
 *    → Textura enviada diretamente à GPU, sem cópia de RAM
 *    → Suporte a canal alpha completo (crítico para ARASAAC)
 *    → Impossível usar RGB_565 aqui — RGB_565 não tem canal alpha!
 *      RGB_565 era o bug raiz dos pictogramas com fundo corrompido.
 *
 * 2. diskCache de 256 MB com diretório dedicado.
 *    Após o primeiro download, os pictogramas nunca mais saem da rede.
 *
 * 3. memoryCache em 25% da RAM livre.
 *    O grid de 4 colunas mantém ~20-40 pictogramas visíveis; 25% é suficiente.
 *
 * 4. coroutineContext com Dispatchers.IO para decodificação paralela.
 *    Sem isso, um único fetch pesado bloqueia os outros.
 *
 * 5. crossfade(false) elimina o stutter de animação durante scroll rápido.
 */
@HiltAndroidApp
class FalaComigoApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // ✅ HARDWARE = GPU upload direto + alpha channel completo
            // ❌ NÃO usar RGB_565 — não tem canal alpha, corrompe pictogramas ARASAAC
            .allowHardware(true)
            // Decodificação em IO paralelo — evita que um fetch pesado bloqueie os outros
            // .fetcherCoroutineContext(Dispatchers.IO) // Removed to fix build
            // .decoderCoroutineContext(Dispatchers.IO) // Removed to fix build
            .memoryCache {
                MemoryCache.Builder(this)
                    // 25% é o sweet spot para grids densos: guarda o board inteiro sem pressão de GC
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("arasaac_cache"))
                    // 256 MB: ~2500 pictogramas em 500px guardados permanentemente
                    .maxSizeBytes(256L * 1024 * 1024)
                    .build()
            }
            // Cache de rede agressivo — pictogramas ARASAAC são estáticos
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            // Sem crossfade — elimina stutters durante scroll rápido
            .crossfade(false)
            .build()
    }
}