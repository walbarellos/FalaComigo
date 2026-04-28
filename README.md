# Fala Comigo — The Apex Engine (v0.4.1)

[![Fala Comigo](https://img.shields.io/badge/Fala-Comigo-007AFF?style=for-the-badge&logo=speech)](https://github.com/walbarellos/FalaComigo)
[![Versão](https://img.shields.io/github/v/release/walbarellos/FalaComigo?include_prereleases&style=flat-square)](https://github.com/walbarellos/FalaComigo/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/walbarellos/FalaComigo/total?style=flat-square)](https://github.com/walbarellos/FalaComigo/releases)
[![Último Commit](https://img.shields.io/github/last-commit/walbarellos/FalaComigo/master?style=flat-square)](https://github.com/walbarellos/FalaComigo/commits/master)
[![Issues Abertas](https://img.shields.io/github/issues/walbarellos/FalaComigo?style=flat-square)](https://github.com/walbarellos/FalaComigo/issues)
[![Stars](https://img.shields.io/github/stars/walbarellos/FalaComigo?style=flat-square)](https://github.com/walbarellos/FalaComigo/stargazers)
[![Licença: GPL v3](https://img.shields.io/badge/licen%C3%A7a-GPL%20v3-blue?style=flat-square)](LICENSE)
---

[![Android](https://img.shields.io/badge/Android-7.0%2B-green?style=for-the-badge&logo=android)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.x-blue?style=for-the-badge)](https://developer.android.com/compose)
[![Hilt](https://img.shields.io/badge/Hilt-2.48.x-orange?style=for-the-badge)](https://dagger.dev/hilt)

---

<div align="center">
  <img src="https://github.com/user-attachments/assets/a79be212-a7ae-45a1-8f89-cc0f626069a7" width="100%" alt="Fala Comigo Header" />
  <br />
  
  <p align="center">
    <b>Transformando comunicação em uma experiência de alta performance.</b>
    <br />
    <i>Desenho direto via GPU • Arquitetura Determinística MVI • Feedback Háptico Sincronizado</i>
  </p>

  <a href="https://github.com/walbarellos/FalaComigo/releases/download/v0.4.1-apex-engine-p3/FalaComigo-v0.4.1-release.apk">
    <img src="https://img.shields.io/badge/DOWNLOAD_LATEST_STABLE_APK-v0.4.1-white?style=for-the-badge&logo=android&logoColor=black&labelColor=3DDC84" alt="Download APK" />
  </a>
  <br />
  <code>SHA-256 (v0.4.1): 6af634b10354658e6ecb2b9e1199ba4babb5b343f5be56e814566910bf920fb2</code>
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
4. Funcionalidades Principais (v0.4.1 Apex P3)
5. Arquitetura e Tecnologias
6. Instalação e Verificação
7. Como Contribuir
8. Roadmap e Versões Futuras
9. Licença e Termos Legais
10. Star History

---

[![Versão do Projeto](https://img.shields.io/badge/vers%C3%A3o-0.4.1-blue?style=flat-square&logo=semver)](https://github.com/walbarellos/FalaComigo/releases)
[![Security: VirusTotal Clean](https://img.shields.io/badge/Seguran%C3%A7a-Auditado-brightgreen?style=flat-square&logo=google-cloud)](https://www.virustotal.com/gui/file/6af634b10354658e6ecb2b9e1199ba4babb5b343f5be56e814566910bf920fb2/detection)
[![License: GPL v3](https://img.shields.io/badge/licen%C3%A7a-GPL%20v3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/gpl-3.0)
[![UI Performance: 120FPS](https://img.shields.io/badge/Performance-120FPS-orange?style=flat-square)](https://github.com/walbarellos/FalaComigo)

---

## 2. Segurança e Integridade (Auditado)

O Fala Comigo v0.4.1 foi submetido a validações de estabilidade, integridade e uso manual para garantir uma experiência segura e previsível.

### Relatório VirusTotal (Google)
O binário oficial anterior (APK) v0.4.0 foi analisado por 70+ motores de antivírus e obteve 0 detecções. A v0.4.1 mantém a mesma linha de assinatura e adiciona correções de estabilidade.

*   [Ver Relatório de Auditoria em Tempo Real](https://www.virustotal.com/gui/file/6af634b10354658e6ecb2b9e1199ba4babb5b343f5be56e814566910bf920fb2/detection)

### Selo de Autenticidade (SHA-256)
Código SHA-256 oficial da v0.4.1: `6af634b10354658e6ecb2b9e1199ba4babb5b343f5be56e814566910bf920fb2`

---

## 3. Visão Geral do Projeto

### O que é o Fala Comigo?
O **Fala Comigo** é um aplicativo brasileiro de Comunicação Aumentativa e Alternativa (CAA) de alta performance. Desenvolvido para pessoas com complexidades de comunicação, ele permite transformar símbolos pictóricos em voz digital com agilidade e fluidez.

### Missão
> "Democratizar o acesso à comunicação através de uma tecnologia soberana, segura e de alto desempenho."

---

## 4. Funcionalidades Principais (v0.4.1 Apex P3)

### Engenharia de Visualização
*   **Apex Engine**: Sistema de renderização manual via GPU (120 FPS) que elimina o atrito de layout do Android.
*   **Múltiplos Modos**: Grade Clássica (Snapping Magnético), Foco (Pager com Parallax) e MMO (Category Stream).

### Orquestração Sensorial
*   **Haptic Sync**: Feedback tátil sincronizado com o scroll magnético.
*   **Áudio Preditivo**: Hardware de som pré-aquecido para fala instantânea.
*   **Offline-First P3**: imagens críticas persistidas localmente, tela de urgência corrigida e bootstrap visual determinístico.

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

### 1. Baixe o APK Assinado
Acesse as [Releases Oficiais](https://github.com/walbarellos/FalaComigo/releases) e baixe o arquivo `FalaComigo-v0.4.1-release.apk`.

### 2. Verifique o Hash
Execute no seu terminal para garantir a integridade:
*   **macOS/Linux**: `sha256sum FalaComigo-v0.4.1-release.apk`
*   **Windows**: `certutil -hashfile FalaComigo-v0.4.1-release.apk SHA256`

---

## 8. Roadmap e Versões Futuras

### Concluído (v0.4.1)
- [x] Motor de renderização Apex (120 FPS).
- [x] Múltiplos modos de visualização (Foco/MMO).
- [x] Orquestração háptica e áudio preditivo.
- [x] Assinatura digital RSA nativa.
- [x] Pipeline offline-first de imagens.
- [x] Correção da tela Urgente e migração Room segura.

---

## 9. Licença e Termos Legais
O **Fala Comigo** é distribuído sob licença **GNU General Public License v3.0 (GPLv3)**.

### Criador do Projeto
| Informação | Detalhe |
|------------|---------|
| **Nome** | Willian Albarello |
| **Email** | willianalbarello@gmail.com |

---

## 10. Star History

<a href="https://www.star-history.com/?repos=walbarellos%2FFalaComigo&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&legend=top-left" />
 </picture>
</a>
