# Histórico de Releases — Fala Comigo

Este documento registra a evolução técnica e funcional do projeto, detalhando os marcos de engenharia e melhorias de UX.

---

## [v0.4.2] — 28 de Abril de 2026 (Strategic Convergence)
Esta release consolida a arquitetura de tokens e limpa o substrate técnico, preparando o app para uma escalabilidade visual sem precedentes.

### 🌟 Design System (Tokens)
- **Implementação de Design Tokens**: Centralização de cores, tipografia, espaçamento e tamanhos em objetos fortemente tipados (`ColorTokens`, `TypeTokens`, etc.).
- **Limpeza de Recursos**: Remoção de temas e strings redundantes em favor da arquitetura de tokens.
- **Substrate Reforçado**: Otimização do `.gitignore` e limpeza de scripts de build legados.

### Performance e estabilidade
- Sincronização da lógica de comunicação com os novos padrões de design.
- Preparação para suporte a múltiplos temas dinâmicos via tokens.

---

## [v0.4.1] — 28 de Abril de 2026 (Apex Engine P3 — Offline-First Stability)
Patch de estabilidade da linha Apex, focado em eliminar pop-in visual, corrigir a tela de urgência e consolidar o pipeline persistente de imagens.

### Correções críticas
- **Tela Urgente corrigida**: a prancha de emergência agora passa pela `CommunicationViewModel` e recebe fallback seguro caso o banco antigo tenha vínculos vazios.
- **Pipeline offline-first de imagens**: símbolos usam `thumbnailPath`/`localImagePath` persistidos em Room antes de recorrer a URL remota.
- **Bootstrap determinístico**: a tela inicial aguarda as imagens críticas visíveis antes de soltar a prancha principal.
- **Migração Room 30 → 31**: adiciona campos de cache local sem `fallbackToDestructiveMigration()`.
- **Editor e picker alinhados**: superfícies auxiliares usam o mesmo resolvedor local-first do renderer principal.

### Performance e estabilidade
- Redução de warm-up/haptic desnecessário na primeira composição.
- Cooldown para `warmUpTts()` durante rolagem rápida.
- Testes JVM ajustados com Robolectric para validar `CommunicationViewModel`.
- Validação manual reportada: 40s de manuseio sem erros fatais/logcat ERROR, RAM saudável e banco ativo.

### Artefatos v0.4.1
- **APK Assinado (Produção)**: `FalaComigo-v0.4.1-release.apk`
- **Tag sugerida**: `v0.4.1-apex-engine-p3`
- **SHA-256 Checksum**: `6af634b10354658e6ecb2b9e1199ba4babb5b343f5be56e814566910bf920fb2`

---

## [v0.4.0] — 28 de Abril de 2026 (The Apex Engine)
A release "Apex". Um salto monumental na Engenharia de Software do app, transformando-o de uma ferramenta utilitária em uma **Prótese Sensorial** operando a 120 FPS.

### 🌟 Inovações de Interface (Phenotype)
- **Múltiplos Modos de Visualização**: Três layouts arquitetados para diferentes cargas cognitivas (via Configurações):
  - **Grade (Clássica):** O layout padrão, agora com "Magnetic Snapping" para alinhamento perfeito.
  - **Foco (Pager):** Concentração absoluta em um símbolo por vez, com efeito *Parallax* (2.5D) e *Visual Peeking* (sombras dos símbolos vizinhos).
  - **MMO (Category Stream):** Visão de alta densidade estilo "Netflix", permitindo explorar múltiplas categorias simultaneamente via scroll horizontal independente.
- **Orquestração Háptica (Haptics)**: O dispositivo agora vibra sutilmente ("clock tick") em sincronia magnética com o scroll da interface, permitindo uso sem olhar para a tela.
- **Áudio Preditivo (Warm-up)**: O motor de voz agora "acorda" o hardware de som do dispositivo frações de segundo *antes* do usuário clicar, eliminando o delay da primeira fala.

### ⚙️ Engenharia de Elite (Substrate)
- **GPU Direct Drawing (Atrito Zero)**: Os Cards de Símbolo foram achatados (Flat Hierarchy). Removemos 100% dos nós filhos de Layout (`Text`, `Image`). Todo o conteúdo agora é processado diretamente no Canvas da GPU (`drawWithCache`), permitindo scroll fluido de 120 FPS mesmo em aparelhos mais antigos.
- **Cérebro Determinístico (MVI)**: Toda a lógica do aplicativo foi refatorada para um padrão Reducer puro. Cliques e scrolls geram Intents que mutam o estado de forma atômica.
- **Isolamento de Cache de Imagens (Coil)**: As imagens agora possuem um preloader inteligente em um pool de threads de ultra-baixa prioridade. Símbolos são mantidos na RAM e no Disco (cache expandido para 512MB), garantindo "Cache Hit" imediato em qualquer transição de tela.
- **Categorias Fortemente Tipadas (Type-Safety)**: Fim do uso de Strings para lógicas de negócio. Adoção de `Value Objects` (`SymbolCategory`) com complexidade de tempo de renderização O(1).
- **Auto-Assinatura Nível Produção**: O app agora compila nativamente APKs assinados e blindados diretamente no fluxo de Build.

### 📦 Artefatos v0.4.0
- **APK Assinado (Produção)**: `FalaComigo-v0.4.0-release.apk`
- **SHA-256 Checksum**: `84b5327b1e4d14d4e75cfd9504f4b5e216f789a60aa649fd1eb7cb87ecc5523a`

---

## [v0.3.1] — 27 de Abril de 2026 (Refinamento de Voz)
A release "Voz com Alma". Foco em humanizar a saída de áudio e garantir que o app fale mesmo em situações adversas.

### Novidades (Features)
- **Granularidade Vocal**: Novos seletores de Tom (Pitch) e Velocidade com presets amigáveis (Lento, Normal, Rápido).
- **Seletor de Vozes**: Agora é possível escolher entre diferentes "personagens" de voz pt-BR instalados no Android.
- **Filtro Offline**: Opção para usar apenas vozes que não dependem de internet, garantindo autonomia total.
- **Auto-Correção Fonética**: Normalização de texto que garante entonação correta em frases essenciais (ex: "Quero água.").

### Engenharia (Substrate)
- **Motor TTS Reativo**: Listener de progresso que permite sincronizar animações visuais com a fala (isSpeaking).
- **Gestão de Cache de Configurações**: Persistência imediata de preferências de voz via SettingsRepository.
- **Recuperação Automática**: Sistema de "shutdown e reboot" automático do motor de voz em caso de falha silenciosa do Android.
- **Deep Linking**: Atalhos diretos para as configurações de acessibilidade do sistema para facilitar a instalação de novos dados de voz.

### Artefatos v0.3.1
- **APK**: FalaComigo-v0.3.1-release.apk

---

## [v0.3.0] — 26 de Abril de 2026 (Consolidação e Agência)
A "Release da Maturidade". Foco total em dar agência ao usuário e blindar a performance do sistema.

### Novidades (Features)
- **Gestor de Grade**: Sistema de reordenação (Drag-and-Drop) baseado em GPU (60 FPS) com feedback háptico e colisão por matriz.
- **Gestão Independente de Conteúdo**: Aba "Gestão" agora separa a criação de palavras (Dicionário Global) da organização de grupos (Rotinas).
- **Filtro de Recentes**: Sistema de rastro de uso que prioriza os 20 itens mais frequentes ou recém-criados.
- **Filtros Dinâmicos**: Categorias (Comer, Lazer, etc.) agora filtram o catálogo global em tempo real, eliminando pranchas vazias.

### Engenharia (Substrate)
- **Banco de Dados v30**: Evolução do esquema Room para suporte a lastUsedAt.
- **Z-Index Dinâmico**: O item arrastado ganha prioridade visual absoluta, flutuando sobre a grade sem glitches.
- **BackHandler Nativo**: Intercepção do botão voltar do Android para navegação interna em sub-pranchas.
- **Selo de Autenticidade**: Introdução do SHA-256 e Auditoria Pública via VirusTotal (Status: 0/71 Limpo).

### Artefatos v0.3.0
- **APK**: FalaComigo-v0.3.0-release.apk
- **SHA-256**: 29b6883deaadce7365cd0be7a329e0cea4e91db75173e454cbab6c56f197cbdb

---

## [v0.2.0] — Abril de 2026 (Performance e UX)
Foco em eliminar engasgos (jank) e refatorar a base de símbolos.

### Novidades
- **Zero Jank Scroll**: Otimização do pipeline de imagem com Coil 2.6.0 e Hardware Bitmaps.
- **Navegação Reativa**: Implementação de Flow e combine para atualizações de UI instantâneas após edições no banco.
- **Expansão de Vocabulário**: Adição de termos de proteção (Roubo, Bater, Denunciar) com IDs ARASAAC verificados.

### Engenharia
- **Ultra-Flat Components**: Refatoração do SymbolCard usando drawBehind para reduzir o overhead de camadas no Compose.
- **Cache Busting**: Sistema de chaves dinâmicas para forçar a atualização de imagens ARASAAC quando os IDs são corrigidos.

---

## [v0.1.0] — Abril de 2025 (MVP)
O nascimento do projeto.

### Funcionalidades Iniciais
- Grid básico 4x4 com símbolos ARASAAC.
- Motor de voz TTS nativo com controle de velocidade.
- Abas de Início, Rotinas e Favoritos (automático).
- Layout em cores pastéis para redução de carga sensorial.

### Limitações da v0.1.0 (Resolvidas em versões posteriores)
- Voz com atraso de inicialização.
- Perda de dados ao desinstalar.
- Filtros estáticos e muitas vezes vazios.

---
*Desenvolvido por Willian Albarello.*
