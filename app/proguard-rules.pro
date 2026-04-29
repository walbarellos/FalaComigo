# ===========================================
# FalaComigo — ProGuard / R8
# Arquivo: app/proguard-rules.pro
#
# Objetivo:
# Manter uma configuração mínima e prudente para release futuro,
# sem inflar regras, sem desativar minificação e sem interferir no build debug.
# ===========================================

# -------------------------------------------
# Atributos necessários para bibliotecas que usam
# anotações, genéricos e reflexão controlada.
# -------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# -------------------------------------------
# Retrofit / Gson
# Preserva metadados necessários para conversores e chamadas HTTP.
# -------------------------------------------
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Mantém interfaces de API remota do app.
-keep interface br.com.falacomigo.data.remote.** { *; }

# Mantém modelos do app usados em mapeamento, serialização ou reflexão.
-keep class br.com.falacomigo.core.model.** { *; }
-keep class br.com.falacomigo.data.remote.** { *; }

# -------------------------------------------
# Room
# Room normalmente gera código necessário via annotation processing/KSP.
# Estas regras mantêm entidades locais do app e reduzem ruído.
# -------------------------------------------
-keep class br.com.falacomigo.data.local.entities.** { *; }
-dontwarn androidx.room.paging.**

# -------------------------------------------
# Hilt / Dagger
# Hilt/Dagger normalmente geram código compatível com R8.
# Evitar warnings opcionais sem manter o app inteiro.
# -------------------------------------------
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# -------------------------------------------
# Coil / Compose
# Compose e Coil costumam funcionar bem com R8.
# Regras abaixo evitam warnings opcionais sem desabilitar otimização.
# -------------------------------------------
-dontwarn coil.**
-dontwarn androidx.compose.**

# -------------------------------------------
# Kotlin / Coroutines
# Redução de ruído em cenários de release.
# -------------------------------------------
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**

# -------------------------------------------
# Regra de prudência:
# Não manter todo o pacote do app sem necessidade.
# Se o release futuro quebrar por minificação, adicionar regra mínima
# apenas para a classe indicada pelo erro real.
# -------------------------------------------
