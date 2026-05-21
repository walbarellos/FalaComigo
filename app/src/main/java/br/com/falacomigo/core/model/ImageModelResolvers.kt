package br.com.falacomigo.core.model

import android.content.Context
import java.io.File

/**
 * Centraliza a lógica de resolução de imagem para garantir consistência em toda a UI.
 * Prioriza: Miniatura -> Local -> Recurso -> URL -> Antigo ImagePath
 */
fun SymbolUiModel.resolveImageModel(
    context: Context,
    preferThumbnail: Boolean = false,
): Any? = when {
    // 1. Miniatura persistente (Otimização para Grids)
    preferThumbnail && thumbnailPath.asExistingImageFile() != null -> thumbnailPath.asExistingImageFile()
    
    // 2. Imagem local em tamanho real (Garantia de Offline)
    localImagePath.asExistingImageFile() != null -> localImagePath.asExistingImageFile()
    
    // 3. Recurso embutido (Símbolos base do app)
    imageResId != 0 -> imageResId
    
    // 4. Fallback para imagePath se for um drawable
    !imagePath.isNullOrBlank() && !imagePath.startsWith("/") -> {
        context.resources
            .getIdentifier(imagePath, "drawable", context.packageName)
            .takeIf { it != 0 }
    }
    
    // 5. URL da ARASAAC (Último recurso, gatilho de cache do Coil)
    !imageUrl.isNullOrBlank() -> imageUrl
    
    // 6. Legado: Caminho absoluto no imagePath
    !imagePath.isNullOrBlank() && imagePath.startsWith("/") -> imagePath.asExistingImageFile()
    
    else -> null
}

private fun String?.asExistingImageFile(): File? {
    if (isNullOrBlank()) return null
    val file = File(this)
    return file.takeIf { it.isFile && it.length() > 0L }
}
