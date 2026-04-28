package br.com.falacomigo.data.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class StoredSymbolImage(
    val fullPath: String,
    val thumbnailPath: String?
)

/**
 * Armazenamento local permanente de imagens de símbolos.
 * Resolve o bug arquitetural da volatilidade do cacheDir.
 */
@Singleton
class SymbolImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val storeDir: File = File(context.filesDir, "symbols").also { it.mkdirs() }
    private val fullDir: File = File(storeDir, "full").also { it.mkdirs() }
    private val thumbDir: File = File(storeDir, "thumbs").also { it.mkdirs() }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val downloadSemaphore = Semaphore(permits = 3)
    private val symbolLocks = ConcurrentHashMap<String, Mutex>()

    /**
     * Retorna o File local do símbolo.
     * Se não existe, baixa, persiste e retorna.
     */
    suspend fun ensureDownloaded(
        symbolId: String, 
        url: String,
        thumbSizePx: Int = 192
    ): StoredSymbolImage? {
        val mutex = symbolLocks.getOrPut(symbolId) { Mutex() }
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                val fullFile = fileFor(symbolId)
                val thumbFile = thumbnailFileFor(symbolId)

                if (fullFile.exists() && fullFile.length() > 0L) {
                    if (!thumbFile.exists() || thumbFile.length() == 0L) {
                        generateThumbnail(fullFile, thumbFile, thumbSizePx)
                    }
                    return@withContext StoredSymbolImage(
                        fullPath = fullFile.absolutePath,
                        thumbnailPath = thumbFile.takeIf { it.exists() }?.absolutePath
                    )
                }

                downloadSemaphore.withPermit {
                    try {
                        val request = Request.Builder().url(url).build()
                        httpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) return@withContext null
                            val body = response.body ?: return@withContext null

                            val temp = File(fullDir, "${symbolId}.tmp")
                            temp.outputStream().use { out ->
                                body.byteStream().copyTo(out)
                            }
                            if (!temp.renameTo(fullFile)) return@withContext null

                            generateThumbnail(fullFile, thumbFile, thumbSizePx)

                            StoredSymbolImage(
                                fullPath = fullFile.absolutePath,
                                thumbnailPath = thumbFile.takeIf { it.exists() }?.absolutePath
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    /**
     * Retorna o File local se o símbolo já foi baixado. (Zero Rede)
     */
    fun getLocalFile(symbolId: String): File? {
        val file = fileFor(symbolId)
        return if (file.exists() && file.length() > 0L) file else null
    }

    fun getThumbnailFile(symbolId: String): File? {
        val file = thumbnailFileFor(symbolId)
        return if (file.exists() && file.length() > 0L) file else null
    }

    fun isDownloaded(symbolId: String): Boolean = getLocalFile(symbolId) != null

    private fun fileFor(symbolId: String): File = File(fullDir, "$symbolId.webp")

    private fun thumbnailFileFor(symbolId: String): File =
        File(thumbDir, "${symbolId}_192.webp")

    private fun generateThumbnail(source: File, target: File, maxSizePx: Int) {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(source.absolutePath, bounds)

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(
                    srcWidth = bounds.outWidth,
                    srcHeight = bounds.outHeight,
                    reqWidth = maxSizePx,
                    reqHeight = maxSizePx
                )
            }

            val bitmap = BitmapFactory.decodeFile(source.absolutePath, options) ?: return
            target.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 82, output)
            }
            bitmap.recycle()
        } catch (e: Exception) {
            // Silently fail thumbnail generation
        }
    }

    private fun calculateInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
