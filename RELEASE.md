# 📋 Histórico de Releases — Fala Comigo

Este documento registra a evolução técnica e funcional do projeto, detalhando os marcos de engenharia e melhorias de UX.

---

## [v0.3.0] — 26 de Abril de 2026 (Consolidação e Agência) 🥒🚀
A "Release da Maturidade". Foco total em dar agência ao usuário e blindar a performance do sistema.

### 🌟 Novidades (Features)
- **Gestor de Grade Suprema**: Sistema de reordenação (Drag-and-Drop) baseado em GPU (60 FPS) com feedback háptico e colisão por matriz.
- **Gestão Independente de Conteúdo**: Aba "Gestão" agora separa a criação de palavras (Dicionário Global) da organização de grupos (Rotinas).
- **Filtro de Recentes**: Sistema de rastro de uso que prioriza os 20 itens mais frequentes ou recém-criados.
- **Filtros Dinâmicos**: Categorias (Comer, Lazer, etc.) agora filtram o catálogo global em tempo real, eliminando pranchas vazias.

### 🛠️ Engenharia (Substrate)
- **Banco de Dados v30**: Evolução do esquema Room para suporte a `lastUsedAt`.
- **Z-Index Dinâmico**: O item arrastado ganha prioridade visual absoluta, flutuando sobre a grade sem glitches.
- **BackHandler Nativo**: Intercepção do botão voltar do Android para navegação interna em sub-pranchas.
- **Selo de Autenticidade**: Introdução do SHA-256 e Auditoria Pública via VirusTotal (Status: 0/71 Limpo).

### 📦 Artefatos v0.3.0
- **APK**: `FalaComigo-v0.3.0-release.apk`
- **SHA-256**: `29b6883deaadce7365cd0be7a329e0cea4e91db75173e454cbab6c56f197cbdb`

---

## [v0.2.0] — Abril de 2026 (Performance e UX) ⚡
Foco em eliminar engasgos (jank) e refatorar a base de símbolos.

### 🌟 Novidades
- **Zero Jank Scroll**: Otimização do pipeline de imagem com Coil 2.6.0 e Hardware Bitmaps.
- **Navegação Reativa**: Implementação de `Flow` e `combine` para atualizações de UI instantâneas após edições no banco.
- **Expansão de Vocabulário**: Adição de termos de proteção (Roubo, Bater, Denunciar) com IDs ARASAAC verificados.

### 🛠️ Engenharia
- **Ultra-Flat Components**: Refatoração do `SymbolCard` usando `drawBehind` para reduzir o overhead de camadas no Compose.
- **Cache Busting**: Sistema de chaves dinâmicas para forçar a atualização de imagens ARASAAC quando os IDs são corrigidos.

---

## [v0.1.0] — Abril de 2025 (MVP) 🐣
O nascimento do projeto.

### 🌟 Funcionalidades Iniciais
- Grid básico 4x4 com símbolos ARASAAC.
- Motor de voz TTS nativo com controle de velocidade.
- Abas de Início, Rotinas e Favoritos (automático).
- Layout em cores pastéis para redução de carga sensorial.

### ⚠️ Limitações da v0.1.0 (Resolvidas em versões posteriores)
- Voz com atraso de inicialização.
- Perda de dados ao desinstalar.
- Filtros estáticos e muitas vezes vazios.

---
*Desenvolvido com rigor técnico e 🥒 por Willian Albarello.*