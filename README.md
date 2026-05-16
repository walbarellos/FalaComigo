# Fala Comigo — The Apex Engine (v0.4.2-beta)

[![Fala Comigo](https://img.shields.io/badge/Fala-Comigo-007AFF?style=for-the-badge&logo=speech)](https://github.com/walbarellos/FalaComigo)
[![Versão](https://img.shields.io/github/v/release/walbarellos/FalaComigo?include_prereleases&style=flat-square)](https://github.com/walbarellos/FalaComigo/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/walbarellos/FalaComigo/total?style=flat-square)](https://github.com/walbarellos/FalaComigo/releases)
[![Último Commit](https://img.shields.io/github/last-commit/walbarellos/FalaComigo/beta?style=flat-square)](https://github.com/walbarellos/FalaComigo/commits/beta)
[![Issues Abertas](https://img.shields.io/github/issues/walbarellos/FalaComigo?style=flat-square)](https://github.com/walbarellos/FalaComigo/issues)
[![Stars](https://img.shields.io/github/stars/walbarellos/FalaComigo?style=flat-square)](https://github.com/walbarellos/FalaComigo/stargazers)
[![Licença: BSL 1.1](https://img.shields.io/badge/licen%C3%A7a-BSL%201.1-orange?style=flat-square)](LICENSE)

---

[![Android](https://img.shields.io/badge/Android-7.0%2B-green?style=for-the-badge&logo=android)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.x-blue?style=for-the-badge)](https://developer.android.com/compose)
[![Hilt](https://img.shields.io/badge/Hilt-2.51.x-orange?style=for-the-badge)](https://dagger.dev/hilt)

---

<div align="center">
  <img src="https://github.com/user-attachments/assets/a79be212-a7ae-45a1-8f89-cc0f626069a7" width="100%" alt="Fala Comigo Header" />
  <br />
  
  <p align="center">
    <b>Transformando comunicação em uma experiênca de alta performance.</b>
    <br />
    <i>Desenho direto via GPU • Arquitetura Determinística MVI • Feedback Háptico Sincronizado</i>
  </p>

  <a href="https://github.com/walbarellos/FalaComigo/blob/beta/FalaComigo-v0.4.2-beta.apk">
    <img src="https://img.shields.io/badge/DOWNLOAD_BETA_APK-v0.4.2-white?style=for-the-badge&logo=android&logoColor=black&labelColor=3DDC84" alt="Download APK" />
  </a>
  <br />
  <code>SHA-256 (v0.4.2-beta): aee65984a68c5306cdbe5ba976a8e0ab0c42f679ad73a8f0da78db720e15a42d</code>
</div>

---

Este arquivo faz parte do projeto **Fala Comigo**.
Para informações completas sobre o projeto, visite nossa [Wiki Oficial](https://github.com/walbarellos/FalaComigo/wiki).

==========================================
Tabela de Conteúdos
==========================================
1. Badge e Status do Projeto
2. Segurança e Integridade (Auditado)
3. Visão Geral do Projeto
4. Funcionalidades Principais (v0.4.2-beta Apex)
5. Arquitetura e Tecnologias
6. Instalação e Verificação
7. Como Contribuir
8. Roadmap e Versões Futuras
9. Licença e Termos Legais
10. Star History

---

[![Versão do Projeto](https://img.shields.io/badge/vers%C3%A3o-0.4.2--beta-blue?style=flat-square&logo=semver)](https://github.com/walbarellos/FalaComigo/releases)
[![Security: VirusTotal Clean](https://img.shields.io/badge/Seguran%C3%A7a-Auditado-brightgreen?style=flat-square&logo=google-cloud)](https://www.virustotal.com/gui/home/upload)
[![License: BSL 1.1](https://img.shields.io/badge/licen%C3%A7a-BSL%201.1-orange.svg?style=flat-square)](LICENSE)
[![UI Performance: 120FPS](https://img.shields.io/badge/Performance-120FPS-orange?style=flat-square)](https://github.com/walbarellos/FalaComigo)

---

## 2. Segurança e Integridade (Auditado)

O Fala Comigo v0.4.2-beta foi submetido a validações de estabilidade e integridade para garantir uma experiência segura. Esta versão beta corrige especificamente o crash de inicialização em builds de produção.

### Selo de Autenticidade (SHA-256)
Código SHA-256 oficial da v0.4.2-beta: `aee65984a68c5306cdbe5ba976a8e0ab0c42f679ad73a8f0da78db720e15a42d`

---

## 3. Visão Geral do Projeto

### O que é o Fala Comigo?
O **Fala Comigo** é um aplicativo brasileiro de Comunicação Aumentativa e Alternativa (CAA) de alta performance. Desenvolvido para pessoas com complexidades de comunicação, ele permite transformar símbolos pictográficos em voz digital com agilidade e fluidez.

### Missão
> "Democratizar o acesso à comunicação através de uma tecnologia soberana, segura e de alto desempenho."

---

## 4. Funcionalidades Principais (v0.4.2-beta Apex)

### Engenharia de Visualização
*   **Apex Engine**: Sistema de renderização Jetpack Compose (120 FPS) otimizado para baixa latência.
*   **Layout Edge-to-Edge**: Integração total com as barras de sistema do Android, respeitando áreas seguras de UI.
*   **Múltiplos Modos**: Grade Clássica (Snapping Magnético), Foco (Pager com Parallax) e MMO (Category Stream).

### Orquestração Sensorial
*   **Haptic Sync**: Feedback tátil sincronizado com o scroll magnético.
*   **Áudio Preditivo**: Hardware de som pré-aquecido para fala instantânea.
*   **Offline-First P3**: Imagens críticas persistidas localmente e bootstrap visual determinístico.

---

## 5. Arquitetura e Tecnologias

| Camada | Tecnologia | Destaque |
|--------|------------|----------|
| **UI** | Jetpack Compose | **GPU Direct Drawing (120 FPS)** |
| **Lógica** | MVI Reducer | Determinismo absoluto de estado |
| **Imagens** | Coil + filesDir | Thumbnails e imagens persistentes local-first |
| **Banco** | Room (v31) | Pipeline reativo com paths locais persistidos |

---

## 6. Instalação e Verificação

### 1. Baixe o APK Beta
Acesse a [Branch Beta](https://github.com/walbarellos/FalaComigo/tree/beta) ou as [Releases Oficiais](https://github.com/walbarellos/FalaComigo/releases) e baixe o arquivo `FalaComigo-v0.4.2-beta.apk`.

### 2. Verifique o Hash
Execute no seu terminal para garantir a integridade:
*   **macOS/Linux**: `sha256sum FalaComigo-v0.4.2-beta.apk`
*   **Windows**: `certutil -hashfile FalaComigo-v0.4.2-beta.apk SHA256`

---

## 8. Roadmap e Versões Futuras

### Concluído (v0.4.2-beta)
- [x] Correção de crash R8/ProGuard (TypeToken).
- [x] Implementação de layout Edge-to-Edge real.
- [x] Padronização cromática do Design System em todas as abas.
- [x] Motor de renderização Apex (120 FPS).
- [x] Pipeline offline-first de imagens.

---

## 9. Licença e Termos Legais
O **Fala Comigo** é distribuído sob licença **Business Source License 1.1 (BSL 1.1)**.

### Termos de Uso
- **Uso não-comercial:** Gratuito e permitido para indivíduos e ONGs.
- **Uso comercial/produção:** Requer licença comercial do Licenciante até 06/05/2030.
- **Pós-Change Date:** A licença converterá automaticamente para **Apache 2.0**.

### Criador do Projeto
| Informação | Detalhe |
|------------|---------|
| **Nome** | Willian Albarello |
| **Email** | willianalbarellos@gmail.com |

---

## 10. Star History

<a href="https://www.star-history.com/?repos=walbarellos%2FFalaComigo&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&legend=top-left" />
 </picture>
</a>
