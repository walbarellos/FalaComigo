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
    preferThumbnail && !thumbnailPath.isNullOrBlank() -> File(thumbnailPath)
    
    // 2. Imagem local em tamanho real (Garantia de Offline)
    !localImagePath.isNullOrBlank() -> File(localImagePath)
    
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
    !imagePath.isNullOrBlank() && imagePath.startsWith("/") -> File(imagePath)
    
    else -> null
}
