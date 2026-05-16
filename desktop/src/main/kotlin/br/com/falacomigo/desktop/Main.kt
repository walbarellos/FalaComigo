package br.com.falacomigo.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.util.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.awt.Toolkit
import java.io.File
import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.ui.graphics.toComposeImageBitmap

data class Symbol(
    val id: String,
    val label: String,
    val category: String,
    val speechText: String = label,
    val imageRef: String? = null,
)

private const val CAT_RECENTES = "Recentes"
private const val CAT_FAVORITOS = "Favoritos"
private const val CAT_PRANCHA = "Prancha"

private val AppBackground = Color(0xFFF0F4FF)
private val SoftBorder = Color(0xFFE8EEFF)
private val TextPrimary = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val TextSoft = Color(0xFF94A3B8)

private data class CatTheme(
    val key: String,
    val label: String,
    val col: Color,
    val bg: Color,
    val border: Color,
    val light: Color,
)

private data class OfflineReadiness(
    val criticalTotal: Int,
    val criticalWithLocalImage: Int,
    val criticalWithFallback: Int,
    val missingCriticalLabels: List<String>,
    val boardSlotsFilled: Int,
    val boardSlotsTotal: Int,
) {
    val isReady: Boolean
        get() = missingCriticalLabels.isEmpty() && criticalTotal > 0
}

private val catThemes: Map<String, CatTheme> = listOf(
    CatTheme("Prancha", "Prancha", Color(0xFF6366F1), Color(0xFFEEF2FF), Color(0xFFC7D2FE), Color(0xFFE0E7FF)),
    CatTheme("Recentes", "Recentes", Color(0xFF64748B), Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFEFF4F8)),
    CatTheme("Favoritos", "Favoritos", Color(0xFFF59E0B), Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0xFFFEF3C7)),
    CatTheme("Necessidades", "Necessidades", Color(0xFF2563EB), Color(0xFFEFF6FF), Color(0xFFBFDBFE), Color(0xFFDBEAFE)),
    CatTheme("Social", "Social", Color(0xFF7C3AED), Color(0xFFF5F3FF), Color(0xFFDDD6FE), Color(0xFFEDE9FE)),
    CatTheme("Identidade", "Identidade", Color(0xFF0F766E), Color(0xFFF0FDFA), Color(0xFF99F6E4), Color(0xFFCCFBF1)),
    CatTheme("Dor e Seguranca", "Dor e Segurança", Color(0xFFDC2626), Color(0xFFFEF2F2), Color(0xFFFECACA), Color(0xFFFEE2E2)),
    CatTheme("Emocoes", "Emoções", Color(0xFFEA580C), Color(0xFFFFF7ED), Color(0xFFFED7AA), Color(0xFFFFEDD5)),
    CatTheme("Numeros", "Números", Color(0xFF0891B2), Color(0xFFECFEFF), Color(0xFFA5F3FC), Color(0xFFCFFAFE)),
).associateBy { it.key }

private fun themeForCategory(category: String): CatTheme {
    return catThemes[category] ?: CatTheme(category, category, Color(0xFF334155), Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFE2E8F0))
}

private enum class AppSection(val title: String) {
    INICIO("Início"),
    GESTAO("Gestão"),
    FAVORITOS("Favoritos"),
}

@Composable
private fun TopActionsBar(
    onOpenSos: () -> Unit,
    onOpenOrganize: () -> Unit,
    onOpenConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logoPainter = remember { DesktopImages.loadPainter(localSym("topbar_app_icon")) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, SoftBorder)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF7367F0)),
                contentAlignment = Alignment.Center
            ) {
                if (logoPainter != null) {
                    Image(
                        painter = logoPainter,
                        contentDescription = "FalaComigo",
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("F", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Column {
                Text("FalaComigo", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("Comunicação Alternativa", color = TextSoft, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderButton("Organizar", onOpenOrganize)
            HeaderButton("Config", onOpenConfig)
            Button(
                onClick = onOpenSos,
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFDC2626),
                    contentColor = Color.White
                )
            ) { Text("SOS", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun HeaderButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White,
            contentColor = TextMuted,
            disabledBackgroundColor = Color.White,
            disabledContentColor = Color(0xFFCBD5E1)
        )
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun CategoryChipsRow(
    categories: List<String>,
    active: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = { it }) { cat ->
            val isActive = cat == active
            val t = themeForCategory(cat)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isActive) t.col else Color(0xFFF1F5F9))
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isActive) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(t.col)
                        )
                    }
                    Text(
                        t.label,
                        color = if (isActive) Color.White else Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineBanner(
    text: String,
    background: Color,
    foreground: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = foreground, fontSize = 12.sp)
    }
}

@Composable
private fun SearchBar(
    query: String,
    onChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppBackground)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(13.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = query,
                onValueChange = onChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (query.isBlank()) {
                Text("Buscar símbolo…", color = Color(0xFFCBD5E1), fontSize = 13.sp)
            }
        }
        HeaderButton("Limpar", onClear, enabled = query.isNotBlank())
    }
}

@Composable
private fun BottomNav(
    section: AppSection,
    onSelect: (AppSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Color(0xFF2563EB)
    BottomNavigation(
        modifier = modifier,
        backgroundColor = Color.White,
        contentColor = Color(0xFF64748B),
        elevation = 0.dp,
    ) {
        AppSection.entries.forEach { s ->
            val selected = section == s
            BottomNavigationItem(
                selected = selected,
                onClick = { onSelect(s) },
                icon = {},
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            s.title,
                            color = if (selected) accent else Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Box(
                            Modifier
                                .padding(top = 6.dp)
                                .height(2.dp)
                                .width(28.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) accent else Color.Transparent)
                        )
                    }
                }
            )
        }
    }
}

private object LocalPrefs {
    private val p = Preferences.userRoot().node("br.com.falacomigo.desktop")
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_RECENTS = "recents"
    private const val KEY_PRANCHA_SLOTS = "prancha_slots"
    private const val PRANCHA_SLOTS_COUNT = 12
    private const val KEY_SPEAK_ON_TAP = "speak_on_tap"
    private const val KEY_SIM_LOADING = "sim_loading"
    private const val KEY_SIM_ERROR = "sim_error"
    private const val KEY_SIM_OFFLINE = "sim_offline"
    private const val KEY_BEEP_ON_TAP = "beep_on_tap"
    private const val KEY_LARGE_CARDS = "large_cards"
    private const val KEY_HIGH_CONTRAST = "high_contrast"
    private const val KEY_EDITOR_PIN = "editor_pin"
    private const val KEY_USAGE_COUNTS = "usage_counts"

    fun loadSet(key: String): Set<String> =
        p.get(key, "").split('|').map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun loadList(key: String): List<String> =
        p.get(key, "").split('|').map { it.trim() }.filter { it.isNotEmpty() }

    fun saveSet(key: String, value: Set<String>) {
        p.put(key, value.joinToString("|"))
    }

    fun saveList(key: String, value: List<String>) {
        p.put(key, value.joinToString("|"))
    }

    fun loadFavorites(): Set<String> = loadSet(KEY_FAVORITES)
    fun saveFavorites(value: Set<String>) = saveSet(KEY_FAVORITES, value)
    fun hasSavedFavorites(): Boolean = p.get(KEY_FAVORITES, null) != null

    fun loadRecents(): List<String> = loadList(KEY_RECENTS)
    fun saveRecents(value: List<String>) = saveList(KEY_RECENTS, value)

    fun loadPranchaSlots(): List<String?> {
        val raw = p.get(KEY_PRANCHA_SLOTS, "")
        if (raw.isBlank()) return List(PRANCHA_SLOTS_COUNT) { null }
        val parts = raw.split('|')
        val slots = parts.take(PRANCHA_SLOTS_COUNT).map { s ->
            val t = s.trim()
            if (t.isEmpty()) null else t
        }.toMutableList()
        while (slots.size < PRANCHA_SLOTS_COUNT) slots.add(null)
        return slots
    }

    fun savePranchaSlots(value: List<String?>) {
        val normalized = value.take(PRANCHA_SLOTS_COUNT) + List(maxOf(0, PRANCHA_SLOTS_COUNT - value.size)) { null }
        p.put(KEY_PRANCHA_SLOTS, normalized.joinToString("|") { it ?: "" })
    }

    fun loadSpeakOnTap(): Boolean = p.getBoolean(KEY_SPEAK_ON_TAP, true)
    fun saveSpeakOnTap(value: Boolean) = p.putBoolean(KEY_SPEAK_ON_TAP, value)

    fun loadSimLoading(): Boolean = p.getBoolean(KEY_SIM_LOADING, false)
    fun saveSimLoading(value: Boolean) = p.putBoolean(KEY_SIM_LOADING, value)

    fun loadSimError(): Boolean = p.getBoolean(KEY_SIM_ERROR, false)
    fun saveSimError(value: Boolean) = p.putBoolean(KEY_SIM_ERROR, value)

    fun loadSimOffline(): Boolean = p.getBoolean(KEY_SIM_OFFLINE, false)
    fun saveSimOffline(value: Boolean) = p.putBoolean(KEY_SIM_OFFLINE, value)

    fun loadBeepOnTap(): Boolean = p.getBoolean(KEY_BEEP_ON_TAP, false)
    fun saveBeepOnTap(value: Boolean) = p.putBoolean(KEY_BEEP_ON_TAP, value)

    fun loadLargeCards(): Boolean = p.getBoolean(KEY_LARGE_CARDS, false)
    fun saveLargeCards(value: Boolean) = p.putBoolean(KEY_LARGE_CARDS, value)

    fun loadHighContrast(): Boolean = p.getBoolean(KEY_HIGH_CONTRAST, false)
    fun saveHighContrast(value: Boolean) = p.putBoolean(KEY_HIGH_CONTRAST, value)

    fun loadEditorPin(): String = p.get(KEY_EDITOR_PIN, "1234")
    fun saveEditorPin(value: String) = p.put(KEY_EDITOR_PIN, value)

    fun loadUsageCounts(): Map<String, Int> {
        return p.get(KEY_USAGE_COUNTS, "")
            .split('|')
            .mapNotNull { part ->
                val pieces = part.split(':', limit = 2)
                val id = pieces.getOrNull(0)?.trim().orEmpty()
                val count = pieces.getOrNull(1)?.toIntOrNull()
                if (id.isNotEmpty() && count != null && count > 0) id to count else null
            }
            .toMap()
    }

    fun saveUsageCounts(value: Map<String, Int>) {
        p.put(
            KEY_USAGE_COUNTS,
            value
                .filterValues { it > 0 }
                .entries
                .sortedByDescending { it.value }
                .joinToString("|") { "${it.key}:${it.value}" }
        )
    }
}

private object DesktopTts {
    // We don't assume TTS is installed on the host. This tries common CLI TTS engines when present.
    // If none exist, speaking becomes a no-op (still keeps the UX flow testable).
    private val candidates: List<List<String>> = listOf(
        listOf("spd-say", "--", "{text}"),
        listOf("espeak-ng", "{text}"),
        listOf("espeak", "{text}"),
        listOf("say", "{text}"), // macOS
    )

    private var cachedCmd: List<String>? = null
    private var cachedChecked = false
    private var currentProcess: Process? = null

    private fun findFirstWorkingCommand(): List<String>? {
        if (cachedChecked) return cachedCmd
        for (cmd in candidates) {
            val exe = cmd.firstOrNull() ?: continue
            val ok = runCatching {
                val p = ProcessBuilder("sh", "-lc", "command -v $exe >/dev/null 2>&1").start()
                p.waitFor() == 0
            }.getOrDefault(false)
            if (ok) return cmd
        }
        return null
    }

    fun isAvailable(): Boolean {
        cachedCmd = findFirstWorkingCommand()
        cachedChecked = true
        return cachedCmd != null
    }

    fun speak(text: String): Boolean {
        if (text.isBlank()) return false
        val cmd = cachedCmd ?: findFirstWorkingCommand()
        cachedCmd = cmd
        cachedChecked = true
        if (cmd == null) return false
        val expanded = cmd.map { it.replace("{text}", text) }
        // Reinicia a fala em vez de sobrepor áudios concorrentes.
        return runCatching {
            currentProcess?.destroy()
            currentProcess = ProcessBuilder(expanded).start()
            true
        }.getOrDefault(false)
    }
}

private const val ANDROID_DRAWABLE_DIR =
    "/home/walbarellos/teste/Docs/docs/FalaComigo-master-antigo/app/src/main/res/drawable-nodpi"

private fun androidSym(name: String): String = "$ANDROID_DRAWABLE_DIR/$name"
private fun androidSymOrNull(name: String): String? {
    val p = androidSym(name)
    return if (File(p).exists()) p else null
}
private fun localSym(name: String): String? = androidSymOrNull("$name.png")

private val baseSeedSymbols = listOf(
    // Base do app antigo funcional, com prioridade para os pictogramas locais existentes.
    Symbol("eu", "Eu", "Identidade", speechText = "Eu", imageRef = localSym("sym_eu")),
    Symbol("voce", "Você", "Identidade", speechText = "Você", imageRef = localSym("sym_voce")),
    Symbol("familia", "Família", "Identidade", speechText = "Minha família"),
    Symbol("medico", "Médico", "Identidade", speechText = "Preciso de um médico"),

    Symbol("agua", "Água", "Necessidades", speechText = "Quero água", imageRef = localSym("sym_agua")),
    Symbol("com_sede", "Com sede", "Necessidades", speechText = "Estou com sede", imageRef = localSym("sym_com_sede")),
    Symbol("com_fome", "Com fome", "Necessidades", speechText = "Estou com fome", imageRef = localSym("sym_com_fome")),
    Symbol("banheiro", "Banheiro", "Necessidades", speechText = "Preciso ir ao banheiro", imageRef = localSym("sym_banheiro")),
    Symbol("calor", "Com calor", "Necessidades", speechText = "Estou com calor"),
    Symbol("frio", "Com frio", "Necessidades", speechText = "Estou com frio"),
    Symbol("cansado", "Cansado", "Necessidades", speechText = "Estou cansado", imageRef = localSym("sym_cansado")),
    Symbol("dormir", "Dormir", "Necessidades", speechText = "Quero dormir"),
    Symbol("remedio", "Remédio", "Necessidades", speechText = "Preciso de remédio"),
    Symbol("escovar", "Escovar", "Necessidades", speechText = "Quero escovar os dentes", imageRef = localSym("sym_escovar_dentes")),
    Symbol("tomar_banho", "Tomar banho", "Necessidades", speechText = "Quero tomar banho", imageRef = localSym("sym_tomar_banho")),

    Symbol("dor", "Dor", "Dor e Seguranca", speechText = "Estou com dor", imageRef = localSym("sym_dor")),
    Symbol("machucado", "Machucado", "Dor e Seguranca", speechText = "Estou machucado", imageRef = localSym("sym_machucado")),
    Symbol("ajuda", "Ajuda", "Dor e Seguranca", speechText = "Preciso de ajuda", imageRef = localSym("sym_ajuda")),
    Symbol("quero_parar", "Quero Parar", "Dor e Seguranca", speechText = "Quero parar", imageRef = localSym("sym_quero_parar")),
    Symbol("denunciar", "Denunciar", "Dor e Seguranca", speechText = "Quero denunciar"),

    Symbol("feliz", "Feliz", "Emocoes", speechText = "Estou feliz", imageRef = localSym("sym_feliz")),
    Symbol("triste", "Triste", "Emocoes", speechText = "Estou triste", imageRef = localSym("sym_triste")),
    Symbol("bravo", "Bravo", "Emocoes", speechText = "Estou bravo", imageRef = localSym("sym_bravo")),
    Symbol("com_medo", "Medo", "Emocoes", speechText = "Estou com medo", imageRef = localSym("sym_com_medo")),
    Symbol("frustrado", "Frustrado", "Emocoes", speechText = "Estou frustrado", imageRef = localSym("sym_frustrado")),
    Symbol("assustado", "Assustado", "Emocoes", speechText = "Estou assustado"),

    Symbol("sim", "Sim", "Social", speechText = "Sim"),
    Symbol("nao", "Não", "Social", speechText = "Não"),
    Symbol("oi", "Oi", "Social", speechText = "Olá"),
    Symbol("tchau", "Tchau", "Social", speechText = "Tchau"),
    Symbol("por_favor", "Por favor", "Social", speechText = "Por favor"),
    Symbol("obrigado", "Obrigado", "Social", speechText = "Obrigado"),
    Symbol("desculpe", "Desculpe", "Social", speechText = "Me desculpe"),
    Symbol("nao_entendi", "Não Entendi", "Social", speechText = "Não entendi"),
    Symbol("bom_dia", "Bom dia", "Social", speechText = "Bom dia"),
    Symbol("boa_noite", "Boa noite", "Social", speechText = "Boa noite"),

    Symbol("mais", "Mais", "Numeros", speechText = "Mais"),
    Symbol("acabou", "Acabou", "Numeros", speechText = "Acabou"),
    Symbol("pouco", "Pouco", "Numeros", speechText = "Só um pouco"),
    Symbol("um", "Um", "Numeros", speechText = "Um"),
    Symbol("dois", "Dois", "Numeros", speechText = "Dois"),
    Symbol("tres", "Três", "Numeros", speechText = "Três"),
    Symbol("quatro", "Quatro", "Numeros", speechText = "Quatro"),
    Symbol("cinco", "Cinco", "Numeros", speechText = "Cinco"),

    Symbol("quero", "Quero", "Social", speechText = "Eu quero"),
    Symbol("nao_quero", "Não quero", "Social", speechText = "Eu não quero"),
)

private val defaultFavoriteIds = setOf("agua", "dor", "quero_parar", "eu", "feliz", "sim", "nao")

private val essentialIds = setOf(
    "agua",
    "com_sede",
    "com_fome",
    "banheiro",
    "dor",
    "machucado",
    "ajuda",
    "quero_parar",
    "com_medo",
    "triste",
    "feliz",
    "eu",
    "voce",
)

private val essentialSlotOrder = listOf(
    "agua",
    "com_sede",
    "com_fome",
    "banheiro",
    "dor",
    "machucado",
    "ajuda",
    "quero_parar",
    "eu",
    "voce",
    "feliz",
    "triste",
)

private fun defaultPranchaSlots(slotCount: Int = 12): List<String?> {
    return List(slotCount) { idx -> essentialSlotOrder.getOrNull(idx) }
}

private fun offlineReadinessFor(
    symbols: List<Symbol>,
    pranchaSlots: List<String?>,
): OfflineReadiness {
    val byId = symbols.associateBy { it.id }
    val critical = essentialIds.mapNotNull { byId[it] }
    val missing = essentialIds.mapNotNull { id -> if (byId[id] == null) id else null }
    val withLocalImage = critical.count { ref ->
        val imageRef = ref.imageRef
        imageRef != null && File(imageRef).exists()
    }
    return OfflineReadiness(
        criticalTotal = critical.size,
        criticalWithLocalImage = withLocalImage,
        criticalWithFallback = critical.size - withLocalImage,
        missingCriticalLabels = missing,
        boardSlotsFilled = pranchaSlots.count { it != null },
        boardSlotsTotal = pranchaSlots.size
    )
}

private object DesktopImages {
    // Minimal, deterministic image loading for desktop testing.
    // If the file doesn't exist or can't be decoded, we fall back to the letter tile.
    fun loadPainter(imageRef: String?): Painter? {
        if (imageRef.isNullOrBlank()) return null
        val f = File(imageRef)
        if (!f.exists() || !f.isFile) return null
        val bytes = runCatching { f.readBytes() }.getOrNull() ?: return null
        val skia = runCatching { SkiaImage.makeFromEncoded(bytes) }.getOrNull() ?: return null
        return BitmapPainter(skia.toComposeImageBitmap())
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "FalaComigo (Desktop - Protótipo)",
    ) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                App()
            }
        }
    }
}

@Composable
private fun App() {
    var phrase by remember { mutableStateOf(listOf<Symbol>()) }
    var favorites by remember {
        mutableStateOf(if (LocalPrefs.hasSavedFavorites()) LocalPrefs.loadFavorites() else defaultFavoriteIds)
    }
    var pranchaSlots by remember { mutableStateOf(LocalPrefs.loadPranchaSlots()) }
    var recents by remember { mutableStateOf(LocalPrefs.loadRecents()) } // most-recent first
    var activeCategory by remember { mutableStateOf(CAT_PRANCHA) }
    val scope = rememberCoroutineScope()
    var section by remember { mutableStateOf(AppSection.INICIO) }
    var speakOnTap by remember { mutableStateOf(LocalPrefs.loadSpeakOnTap()) }
    var lastAction by remember { mutableStateOf<String?>(null) }
    var lastActionAt by remember { mutableLongStateOf(0L) }
    var isSpeaking by remember { mutableStateOf(false) }
    var showSos by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }
    var showEditorGate by remember { mutableStateOf(false) }
    var showTtsHealth by remember { mutableStateOf(false) }
    var showOfflineReadiness by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var editorUnlocked by remember { mutableStateOf(false) }
    var editorPin by remember { mutableStateOf(LocalPrefs.loadEditorPin()) }
    var usageCounts by remember { mutableStateOf(LocalPrefs.loadUsageCounts()) }
    var simLoading by remember { mutableStateOf(LocalPrefs.loadSimLoading()) }
    var simError by remember { mutableStateOf(LocalPrefs.loadSimError()) }
    var simOffline by remember { mutableStateOf(LocalPrefs.loadSimOffline()) }
    var beepOnTap by remember { mutableStateOf(LocalPrefs.loadBeepOnTap()) }
    var lastTappedSymbolId by remember { mutableStateOf<String?>(null) }
    var largeCards by remember { mutableStateOf(LocalPrefs.loadLargeCards()) }
    var highContrast by remember { mutableStateOf(LocalPrefs.loadHighContrast()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(favorites) { LocalPrefs.saveFavorites(favorites) }
    LaunchedEffect(recents) { LocalPrefs.saveRecents(recents) }
    LaunchedEffect(pranchaSlots) { LocalPrefs.savePranchaSlots(pranchaSlots) }
    LaunchedEffect(speakOnTap) { LocalPrefs.saveSpeakOnTap(speakOnTap) }
    LaunchedEffect(simLoading) { LocalPrefs.saveSimLoading(simLoading) }
    LaunchedEffect(simError) { LocalPrefs.saveSimError(simError) }
    LaunchedEffect(simOffline) { LocalPrefs.saveSimOffline(simOffline) }
    LaunchedEffect(beepOnTap) { LocalPrefs.saveBeepOnTap(beepOnTap) }
    LaunchedEffect(largeCards) { LocalPrefs.saveLargeCards(largeCards) }
    LaunchedEffect(highContrast) { LocalPrefs.saveHighContrast(highContrast) }
    LaunchedEffect(editorPin) { LocalPrefs.saveEditorPin(editorPin.ifBlank { "1234" }) }
    LaunchedEffect(usageCounts) { LocalPrefs.saveUsageCounts(usageCounts) }
    LaunchedEffect(lastActionAt) {
        if (lastActionAt > 0L && lastAction != null) {
            delay(1600)
            lastAction = null
        }
    }

    LaunchedEffect(Unit) {
        // Doc 061/040: prancha nao deve iniciar vazia no MVP.
        if (pranchaSlots.all { it == null }) {
            pranchaSlots = defaultPranchaSlots(pranchaSlots.size)
        }
    }

    val categories = remember {
        // Doc 061: categorias/areas previsiveis no topo, evitando labirinto.
        val fixed = listOf(
            CAT_PRANCHA,
            CAT_RECENTES,
            CAT_FAVORITOS,
            "Necessidades",
            "Social",
            "Identidade",
            "Dor e Seguranca",
            "Emocoes",
            "Numeros"
        )
        fixed + baseSeedSymbols.map { it.category }.distinct().filterNot { it in fixed }
    }

    val symbolById = remember { baseSeedSymbols.associateBy { it.id } }
    val offlineReadiness = remember(pranchaSlots) { offlineReadinessFor(baseSeedSymbols, pranchaSlots) }
    val openGestao: () -> Unit = {
        if (editorUnlocked) {
            section = AppSection.GESTAO
        } else {
            showEditorGate = true
        }
    }

    val symbols = remember(activeCategory, favorites, recents, pranchaSlots) {
        when (activeCategory) {
            CAT_PRANCHA -> pranchaSlots.mapNotNull { id -> id?.let { symbolById[it] } }
            CAT_FAVORITOS -> baseSeedSymbols.filter { favorites.contains(it.id) }
            CAT_RECENTES -> recents.mapNotNull { symbolById[it] }
            else -> baseSeedSymbols.filter { it.category == activeCategory }
        }
    }

    Column(Modifier.fillMaxSize().background(AppBackground)) {
        TopActionsBar(
            onOpenSos = { showSos = true },
            onOpenOrganize = openGestao,
            onOpenConfig = { showConfig = true },
        )

        if (showSos) {
            SosDialog(
                onSpeak = { label, speech ->
                    isSpeaking = true
                    scope.launch(Dispatchers.IO) {
                        val ok = DesktopTts.speak(speech)
                        lastAction = if (ok) "Falando: $label" else "TTS indisponível"
                        lastActionAt = System.currentTimeMillis()
                        delay(900)
                        isSpeaking = false
                    }
                    lastActionAt = System.currentTimeMillis()
                },
                onClose = { showSos = false }
            )
        }

        if (showConfig) {
            ConfigDialog(
                speakOnTap = speakOnTap,
                onToggleSpeakOnTap = { speakOnTap = !speakOnTap },
                onTestVoice = {
                    isSpeaking = true
                    scope.launch(Dispatchers.IO) {
                        val ok = DesktopTts.speak("Teste de voz")
                        lastAction = if (ok) "Falando: Teste de voz" else "TTS indisponível"
                        lastActionAt = System.currentTimeMillis()
                        delay(900)
                        isSpeaking = false
                    }
                },
                simLoading = simLoading,
                onToggleSimLoading = { simLoading = !simLoading },
                simError = simError,
                onToggleSimError = { simError = !simError },
                simOffline = simOffline,
                onToggleSimOffline = { simOffline = !simOffline },
                beepOnTap = beepOnTap,
                onToggleBeepOnTap = { beepOnTap = !beepOnTap },
                largeCards = largeCards,
                onToggleLargeCards = { largeCards = !largeCards },
                highContrast = highContrast,
                onToggleHighContrast = { highContrast = !highContrast },
                onOpenTtsHealth = {
                    showConfig = false
                    showTtsHealth = true
                },
                onOpenOfflineReadiness = {
                    showConfig = false
                    showOfflineReadiness = true
                },
                onOpenEditorGate = {
                    showConfig = false
                    openGestao()
                },
                onOpenAbout = {
                    showConfig = false
                    showAbout = true
                },
                onClose = { showConfig = false }
            )
        }

        if (showEditorGate) {
            EditorGateDialog(
                expectedPin = editorPin.ifBlank { "1234" },
                onClose = { showEditorGate = false },
                onUnlocked = {
                    editorUnlocked = true
                    showEditorGate = false
                    section = AppSection.GESTAO
                }
            )
        }

        if (showTtsHealth) {
            TtsHealthDialog(
                onTestVoice = {
                    isSpeaking = true
                    scope.launch(Dispatchers.IO) {
                        val ok = DesktopTts.speak("Teste de voz do FalaComigo")
                        lastAction = if (ok) "Falando: teste de voz" else "TTS indisponível"
                        lastActionAt = System.currentTimeMillis()
                        delay(900)
                        isSpeaking = false
                    }
                },
                onClose = { showTtsHealth = false }
            )
        }

        if (showOfflineReadiness) {
            OfflineReadinessDialog(
                readiness = offlineReadiness,
                onUseCriticalBoard = {
                    activeCategory = CAT_PRANCHA
                    section = AppSection.INICIO
                    showOfflineReadiness = false
                },
                onClose = { showOfflineReadiness = false }
            )
        }

        if (showAbout) {
            AboutDialog(onClose = { showAbout = false })
        }

        PhraseBar(
            phrase = phrase,
            statusText = lastAction,
            isSpeaking = isSpeaking,
            onRemoveAt = { index -> phrase = phrase.filterIndexed { i, _ -> i != index } },
            onBackspace = { if (phrase.isNotEmpty()) phrase = phrase.dropLast(1) },
            onClear = { phrase = emptyList() },
            onSpeak = {
                val text = phrase.joinToString(" ") { it.speechText }
                isSpeaking = true
                scope.launch(Dispatchers.IO) {
                    val ok = DesktopTts.speak(text)
                    lastAction = if (ok) "Falando frase" else "TTS indisponível"
                    lastActionAt = System.currentTimeMillis()
                    delay(900)
                    isSpeaking = false
                }
            }
        )

        Box(Modifier.weight(1f)) {
        when (section) {
            AppSection.INICIO -> {
                Column(Modifier.fillMaxSize()) {
                    CategoryChipsRow(
                        categories = categories,
                        active = activeCategory,
                        onSelect = { activeCategory = it },
                    )
                    SearchBar(
                        query = searchQuery,
                        onChange = { searchQuery = it },
                        onClear = { searchQuery = "" },
                    )
                    if (simOffline) {
                        InlineBanner(
                            text = "Sem internet. A comunicação básica continua disponível.",
                            background = Color(0xFF6E5200)
                        )
                    }

                    if (simError) {
                        InlineBanner(
                            text = "Não consegui carregar agora. Tente novamente.",
                            background = Color(0xFF8B1A1A)
                        )
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { simError = false }) { Text("Tentar novamente") }
                            Button(onClick = { activeCategory = "Necessidades" }) { Text("Ir para básico") }
                        }
                    }

                    if (simLoading) {
                        InlineBanner(
                            text = "Preparando símbolos…",
                            background = Color(0xFF1F6FEB)
                        )
                    }

                    val effectiveSymbols = when {
                        simError || simLoading || simOffline -> baseSeedSymbols.filter { essentialIds.contains(it.id) }
                        else -> symbols
                    }

                    val filtered = remember(effectiveSymbols, searchQuery) {
                        if (searchQuery.isBlank()) effectiveSymbols
                        else effectiveSymbols.filter { it.label.contains(searchQuery, ignoreCase = true) }
                    }
                    val emptyMessage = when {
                        searchQuery.isNotBlank() -> "Sem resultado. Tente outro termo."
                        activeCategory == CAT_RECENTES -> "Os símbolos usados aparecerão aqui."
                        activeCategory == CAT_FAVORITOS -> "Os favoritos aparecerão aqui quando forem configurados."
                        activeCategory == CAT_PRANCHA -> "Não encontrei símbolos para mostrar agora."
                        else -> "Ainda não há símbolos aqui."
                    }

                    if (searchQuery.isNotBlank() && filtered.isEmpty()) {
                        InlineBanner(
                            text = "Sem resultado. Tente outro termo.",
                            background = Color(0xFF334155),
                        )
                    }

                    SymbolGrid(
                        symbols = filtered,
                        emptyMessage = emptyMessage,
                        lastTappedSymbolId = lastTappedSymbolId,
                        largeCards = largeCards,
                        highContrast = highContrast,
                        onTap = {
                            lastTappedSymbolId = it.id
                            scope.launch { delay(180); if (lastTappedSymbolId == it.id) lastTappedSymbolId = null }
                            if (beepOnTap) runCatching { Toolkit.getDefaultToolkit().beep() }
                            phrase = phrase + it
                            recents = (listOf(it.id) + recents.filterNot { id -> id == it.id }).take(12)
                            usageCounts = usageCounts + (it.id to ((usageCounts[it.id] ?: 0) + 1))
                            lastAction = "Toque: ${it.label}"
                            lastActionAt = System.currentTimeMillis()
                            if (speakOnTap) {
                                isSpeaking = true
                                scope.launch(Dispatchers.IO) {
                                    val ok = DesktopTts.speak(it.speechText)
                                    lastAction = if (ok) "Falando: ${it.label}" else "TTS indisponível"
                                    lastActionAt = System.currentTimeMillis()
                                    delay(900)
                                    isSpeaking = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            AppSection.FAVORITOS -> {
                val favoriteSymbols = baseSeedSymbols.filter { favorites.contains(it.id) }
                SymbolGrid(
                    symbols = favoriteSymbols,
                    emptyMessage = "Os favoritos aparecerão aqui quando forem configurados.",
                    lastTappedSymbolId = lastTappedSymbolId,
                    largeCards = largeCards,
                    highContrast = highContrast,
                    onTap = {
                            lastTappedSymbolId = it.id
                            scope.launch { delay(180); if (lastTappedSymbolId == it.id) lastTappedSymbolId = null }
                            if (beepOnTap) runCatching { Toolkit.getDefaultToolkit().beep() }
                        phrase = phrase + it
                        recents = (listOf(it.id) + recents.filterNot { id -> id == it.id }).take(12)
                        usageCounts = usageCounts + (it.id to ((usageCounts[it.id] ?: 0) + 1))
                        lastAction = "Toque: ${it.label}"
                        lastActionAt = System.currentTimeMillis()
                        if (speakOnTap) {
                            isSpeaking = true
                            scope.launch(Dispatchers.IO) {
                                val ok = DesktopTts.speak(it.speechText)
                                lastAction = if (ok) "Falando: ${it.label}" else "TTS indisponível"
                                lastActionAt = System.currentTimeMillis()
                                delay(900)
                                isSpeaking = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            AppSection.GESTAO -> {
                // Minimal management surface: clear prancha and clear recents.
                GestaoPanel(
                    pranchaSlots = pranchaSlots,
                    allSymbols = baseSeedSymbols,
                    symbolById = symbolById,
                    favorites = favorites,
                    usageCounts = usageCounts,
                    editorPin = editorPin,
                    onRestorePrancha = { pranchaSlots = defaultPranchaSlots(pranchaSlots.size) },
                    onClearRecents = { recents = emptyList() },
                    onClearUsageHistory = { usageCounts = emptyMap() },
                    onClearFavorites = { favorites = emptySet() },
                    onSetSlot = { slotIndex, symbolIdOrNull ->
                        val next = pranchaSlots.toMutableList()
                        next[slotIndex] = symbolIdOrNull
                        pranchaSlots = next
                    },
                    onToggleFavorite = { symbolId ->
                        favorites = if (favorites.contains(symbolId)) favorites - symbolId else favorites + symbolId
                    },
                    onChangeEditorPin = { nextPin ->
                        editorPin = nextPin.filter { it.isDigit() }.take(8).ifBlank { "1234" }
                    },
                    speakOnTap = speakOnTap,
                    onSetSpeakOnTap = { speakOnTap = it },
                    onJumpToInicio = { section = AppSection.INICIO },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }}

        BottomNav(section = section, onSelect = { next ->
            if (next == AppSection.GESTAO) openGestao() else section = next
        })
    }
}

@Composable
private fun GestaoPanel(
    pranchaSlots: List<String?>,
    allSymbols: List<Symbol>,
    symbolById: Map<String, Symbol>,
    favorites: Set<String>,
    usageCounts: Map<String, Int>,
    editorPin: String,
    onRestorePrancha: () -> Unit,
    onClearRecents: () -> Unit,
    onClearUsageHistory: () -> Unit,
    onClearFavorites: () -> Unit,
    onSetSlot: (Int, String?) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onChangeEditorPin: (String) -> Unit,
    speakOnTap: Boolean,
    onSetSpeakOnTap: (Boolean) -> Unit,
    onJumpToInicio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Gestão", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
        Text(
            "Prancha: ${pranchaSlots.count { it != null }} / ${pranchaSlots.size} slots preenchidos",
            color = TextMuted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRestorePrancha) { Text("Restaurar Prancha") }
            Button(onClick = onClearRecents) { Text("Limpar Recentes") }
            Button(onClick = onClearUsageHistory) { Text("Limpar Histórico") }
            Button(onClick = onClearFavorites) { Text("Limpar Favoritos") }
            Button(onClick = onJumpToInicio) { Text("Voltar ao Início") }
        }

        Text("Preferências", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onSetSpeakOnTap(!speakOnTap) }) {
                Text(if (speakOnTap) "Falar ao tocar: ON" else "Falar ao tocar: OFF")
            }
        }

        Text("PIN da Gestão", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text("Use apenas números. O PIN protege edição acidental da prancha.", fontSize = 12.sp, color = TextMuted)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, SoftBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = editorPin,
                onValueChange = onChangeEditorPin,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (editorPin.isBlank()) {
                Text("PIN", color = Color(0xFFCBD5E1))
            }
        }

        Text("Histórico local", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        val mostUsed = usageCounts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(8)
            .mapNotNull { entry -> symbolById[entry.key]?.let { it to entry.value } }
        if (mostUsed.isEmpty()) {
            Text("Os símbolos usados aparecerão aqui.", fontSize = 12.sp, color = TextMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                mostUsed.forEach { (symbol, count) ->
                    val t = themeForCategory(symbol.category)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, t.border, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(999.dp)).background(t.col))
                            Column {
                                Text(symbol.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(t.label, fontSize = 11.sp, color = TextSoft)
                            }
                        }
                        Text("$count usos", color = t.col, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("Favoritos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(
            "Selecionados: ${favorites.size}",
            fontSize = 12.sp,
            color = TextMuted
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            allSymbols.forEach { s ->
                val isFavorite = favorites.contains(s.id)
                val t = themeForCategory(s.category)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, t.border, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(999.dp)).background(t.col))
                        Column {
                            Text(s.label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(t.label, fontSize = 11.sp, color = TextSoft)
                        }
                    }
                    Button(onClick = { onToggleFavorite(s.id) }) {
                        Text(if (isFavorite) "Remover" else "Favoritar")
                    }
                }
            }
        }

        Text("Editar Prancha (slots)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pranchaSlots.forEachIndexed { idx, id ->
                val currentLabel = id?.let { symbolById[it]?.label } ?: "(vazio)"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Slot ${idx + 1}", fontWeight = FontWeight.SemiBold)
                    Text(currentLabel)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onSetSlot(idx, null) }) { Text("Esvaziar") }
                        Button(onClick = {
                            // Simple cycle: set next symbol in list (fast UX for PC testing).
                            val currentIndex = allSymbols.indexOfFirst { it.id == id }
                            val next = if (currentIndex < 0) allSymbols.firstOrNull() else allSymbols.getOrNull(currentIndex + 1)
                            onSetSlot(idx, next?.id)
                        }) { Text("Próximo") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SosDialog(
    onSpeak: (String, String) -> Unit,
    onClose: () -> Unit,
) {
    val emergency = listOf(
        "Ajuda" to "Ajuda",
        "Dor" to "Estou com dor",
        "Quero Parar" to "Quero parar",
        "Água" to "Quero água",
    )

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("SOS", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Escolha uma fala rápida de emergência.")
                emergency.forEach { (label, speech) ->
                    Button(
                        onClick = { onSpeak(label, speech) },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (label == "Ajuda") Color(0xFFDC2626) else Color(0xFFFEF2F2),
                            contentColor = if (label == "Ajuda") Color.White else Color(0xFF991B1B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onClose) { Text("Fechar") }
        }
    )
}

@Composable
private fun EditorGateDialog(
    expectedPin: String,
    onClose: () -> Unit,
    onUnlocked: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var denied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Área do Cuidador", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Digite o PIN para editar pranchas e favoritos.", color = TextMuted)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, if (denied) Color(0xFFDC2626) else SoftBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = pin,
                        onValueChange = {
                            pin = it.take(8).filter { ch -> ch.isDigit() }
                            denied = false
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pin.isBlank()) {
                        Text("PIN", color = Color(0xFFCBD5E1))
                    }
                }
                if (denied) {
                    Text("PIN incorreto.", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin == expectedPin) onUnlocked() else denied = true
                },
                enabled = pin.isNotBlank()
            ) {
                Text("Entrar")
            }
        },
        dismissButton = {
            Button(onClick = onClose) { Text("Voltar") }
        }
    )
}

@Composable
private fun TtsHealthDialog(
    onTestVoice: () -> Unit,
    onClose: () -> Unit,
) {
    val available = DesktopTts.isAvailable()
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Diagnóstico de voz", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DiagnosticRow("TTS", if (available) "Disponível" else "Indisponível", available)
                DiagnosticRow("Idioma", "pt-BR priorizado", true)
                DiagnosticRow("Fallback", "A prancha continua utilizável sem voz", true)
                Text(
                    if (available) {
                        "O app encontrou um motor de voz local no computador."
                    } else {
                        "Não encontrei um motor de voz local. A frase continua sendo montada na tela."
                    },
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onTestVoice, enabled = available) { Text("Testar voz") }
        },
        dismissButton = {
            Button(onClick = onClose) { Text("Fechar") }
        }
    )
}

@Composable
private fun OfflineReadinessDialog(
    readiness: OfflineReadiness,
    onUseCriticalBoard: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Diagnóstico offline", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DiagnosticRow(
                    "Símbolos críticos",
                    "${readiness.criticalTotal} disponíveis",
                    readiness.missingCriticalLabels.isEmpty()
                )
                DiagnosticRow(
                    "Imagens locais",
                    "${readiness.criticalWithLocalImage} locais / ${readiness.criticalWithFallback} com fallback",
                    readiness.criticalTotal > 0
                )
                DiagnosticRow(
                    "Prancha",
                    "${readiness.boardSlotsFilled}/${readiness.boardSlotsTotal} slots",
                    readiness.boardSlotsFilled > 0
                )
                if (readiness.missingCriticalLabels.isNotEmpty()) {
                    Text(
                        "Faltando: ${readiness.missingCriticalLabels.joinToString(", ")}",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text("A comunicação essencial permanece acessível.", color = TextMuted, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onUseCriticalBoard) { Text("Ver prancha") }
        },
        dismissButton = {
            Button(onClick = onClose) { Text("Fechar") }
        }
    )
}

@Composable
private fun AboutDialog(onClose: () -> Unit) {
    val logoPainter = remember { DesktopImages.loadPainter(localSym("topbar_app_icon")) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Sobre e licenças", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .height(440.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoPainter != null) {
                        Image(
                            painter = logoPainter,
                            contentDescription = "FalaComigo",
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("F", color = Color(0xFF6366F1), fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    }
                }

                Text("FalaComigo", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text(
                    "Comunicação Aumentativa e Alternativa para transformar símbolos em fala, com foco em acessibilidade e uso offline essencial.",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                InfoBlock(
                    title = "Desenvolvimento",
                    body = "Willian Albarello\nGitHub: walbarellos\nContato: willian@falacomigo.app"
                )
                InfoBlock(
                    title = "Símbolos",
                    body = "Pictogramas ARASAAC licenciados sob CC BY-NC-SA. Símbolos locais e fallback visual são priorizados para uso offline."
                )
                InfoBlock(
                    title = "Escopo",
                    body = "Ferramenta de apoio à comunicação assistiva. Não é diagnóstico, tratamento, prontuário, prescrição ou laudo."
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onClose) { Text("Fechar") }
        }
    )
}

@Composable
private fun InfoBlock(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, SoftBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(title, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(body, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    ok: Boolean,
) {
    val color = if (ok) Color(0xFF0F766E) else Color(0xFFDC2626)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (ok) Color(0xFFF0FDFA) else Color(0xFFFEF2F2))
            .border(1.dp, if (ok) Color(0xFF99F6E4) else Color(0xFFFECACA), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun ConfigDialog(
    speakOnTap: Boolean,
    onToggleSpeakOnTap: () -> Unit,
    onTestVoice: () -> Unit,
    simLoading: Boolean,
    onToggleSimLoading: () -> Unit,
    simError: Boolean,
    onToggleSimError: () -> Unit,
    simOffline: Boolean,
    onToggleSimOffline: () -> Unit,
    beepOnTap: Boolean,
    onToggleBeepOnTap: () -> Unit,
    largeCards: Boolean,
    onToggleLargeCards: () -> Unit,
    highContrast: Boolean,
    onToggleHighContrast: () -> Unit,
    onOpenTtsHealth: () -> Unit,
    onOpenOfflineReadiness: () -> Unit,
    onOpenEditorGate: () -> Unit,
    onOpenAbout: () -> Unit,
    onClose: () -> Unit,
) {
    val ttsAvailable = DesktopTts.isAvailable()
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Config") },
        text = {
            Column(
                modifier = Modifier
                    .height(520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Preferências de uso.")
                Button(onClick = onToggleSpeakOnTap) {
                    Text(if (speakOnTap) "Falar ao tocar: ON" else "Falar ao tocar: OFF")
                }
                Text(if (ttsAvailable) "TTS: disponível" else "TTS: indisponível")
                Button(onClick = onTestVoice, enabled = ttsAvailable) { Text("Testar voz") }
                Button(onClick = onOpenTtsHealth) { Text("Diagnóstico de voz") }
                Button(onClick = onOpenOfflineReadiness) { Text("Diagnóstico offline") }

                Text("Estados de interface (QA)", fontWeight = FontWeight.SemiBold)
                Button(onClick = onToggleSimLoading) {
                    Text(if (simLoading) "Simular carregamento: ON" else "Simular carregamento: OFF")
                }
                Button(onClick = onToggleSimError) {
                    Text(if (simError) "Simular erro: ON" else "Simular erro: OFF")
                }
                Button(onClick = onToggleSimOffline) {
                    Text(if (simOffline) "Simular offline: ON" else "Simular offline: OFF")
                }

                Text("Microinteracoes (QA)", fontWeight = FontWeight.SemiBold)
                Button(onClick = onToggleBeepOnTap) {
                    Text(if (beepOnTap) "Beep no toque: ON" else "Beep no toque: OFF")
                }

                Text("Acessibilidade (MVP)", fontWeight = FontWeight.SemiBold)
                Button(onClick = onToggleLargeCards) {
                    Text(if (largeCards) "Cards grandes: ON" else "Cards grandes: OFF")
                }
                Button(onClick = onToggleHighContrast) {
                    Text(if (highContrast) "Alto contraste: ON" else "Alto contraste: OFF")
                }

                Text("Administração", fontWeight = FontWeight.SemiBold)
                Button(onClick = onOpenEditorGate) { Text("Entrar na Gestão") }

                Text("Informações", fontWeight = FontWeight.SemiBold)
                Button(onClick = onOpenAbout) { Text("Sobre e licenças") }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onClose) { Text("Fechar") }
        }
    )
}

@Composable
private fun PhraseBar(
    phrase: List<Symbol>,
    statusText: String?,
    isSpeaking: Boolean,
    onRemoveAt: (Int) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSpeak: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, SoftBorder)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("FRASE", fontSize = 11.sp, color = TextSoft, fontWeight = FontWeight.ExtraBold)
            if (isSpeaking) SpeakingBars()
            Spacer(Modifier.weight(1f))
            HeaderButton("Apagar", onBackspace, enabled = phrase.isNotEmpty())
            Button(
                onClick = onSpeak,
                enabled = phrase.isNotEmpty(),
                shape = RoundedCornerShape(9.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6366F1),
                    contentColor = Color.White,
                    disabledBackgroundColor = Color(0xFFE2E8F0),
                    disabledContentColor = TextSoft
                )
            ) {
                Text("Falar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            HeaderButton("Limpar", onClear, enabled = phrase.isNotEmpty())
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, SoftBorder, RoundedCornerShape(13.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (phrase.isEmpty()) {
                Text(
                    "Toque nos símbolos para montar uma frase...",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(phrase.size, key = { it }) { index ->
                        val symbol = phrase[index]
                        val t = themeForCategory(symbol.category)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(t.bg)
                                .border(1.dp, t.border, RoundedCornerShape(22.dp))
                                .clickable { onRemoveAt(index) }
                                .padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(t.col)
                            )
                            Text(symbol.label, color = t.col, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (!statusText.isNullOrBlank()) {
            Text(statusText, fontSize = 12.sp, color = Color(0xFF2563EB))
        }
    }
}

@Composable
private fun SpeakingBars() {
    val transition = rememberInfiniteTransition(label = "speaking")
    val heights = List(3) { index ->
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 420, delayMillis = index * 110),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }
    Row(
        modifier = Modifier.height(18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((6 + 12 * h.value).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF2563EB))
            )
        }
    }
}

@Composable
private fun SymbolGrid(
    symbols: List<Symbol>,
    emptyMessage: String,
    lastTappedSymbolId: String?,
    largeCards: Boolean,
    highContrast: Boolean,
    onTap: (Symbol) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (symbols.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, color = TextSoft, fontWeight = FontWeight.SemiBold)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = if (largeCards) 220.dp else 160.dp),
        modifier = modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(symbols.size, key = { idx -> "${idx}:${symbols[idx].id}" }) { idx ->
            val s = symbols[idx]
            SymbolTile(
                symbol = s,
                isJustTapped = lastTappedSymbolId == s.id,
                highContrast = highContrast,
                largeCards = largeCards,
                onTap = { onTap(s) }
            )
        }
    }
}

@Composable
private fun SymbolTile(
    symbol: Symbol,
    isJustTapped: Boolean,
    largeCards: Boolean,
    highContrast: Boolean,
    onTap: () -> Unit,
) {
    val t = themeForCategory(symbol.category)
    val imageBg by animateColorAsState(
        targetValue = when {
            highContrast -> Color.White
            isJustTapped -> t.light
            else -> t.bg
        },
        animationSpec = tween(durationMillis = 120),
        label = "tileBg"
    )
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .height(if (largeCards) 198.dp else 166.dp)
            .shadow(3.dp, shape, clip = false)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, t.border, shape)
            .clickable(onClick = onTap)
            .padding(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (largeCards) 122.dp else 96.dp)
                .background(imageBg)
        ) {
            val painter = remember(symbol.imageRef) { DesktopImages.loadPainter(symbol.imageRef) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(t.col)
            )

            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = symbol.label,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(if (largeCards) 0.82f else 0.78f),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(if (largeCards) 72.dp else 58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.88f))
                        .border(1.dp, t.border, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        symbol.label.take(2).uppercase(),
                        color = t.col,
                        fontSize = if (largeCards) 24.sp else 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(symbol.label, color = TextPrimary, fontSize = if (largeCards) 18.sp else 15.sp, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(7.dp).clip(RoundedCornerShape(999.dp)).background(t.col))
                Text(t.label.uppercase(), color = TextSoft, fontSize = if (largeCards) 12.sp else 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
// (intencionalmente vazio)
