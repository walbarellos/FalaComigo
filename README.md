# 📢 FalaComigo Beta v0.4.2

[![Fala Comigo](https://img.shields.io/badge/Fala-Comigo-007AFF?style=for-the-badge&logo=speech)](https://github.com/walbarellos/FalaComigo)
[![Versão](https://img.shields.io/badge/vers%C3%A3o-0.4.2--beta-blue?style=for-the-badge&logo=semver)](https://github.com/walbarellos/FalaComigo/releases)
[![Licença: BSL 1.1](https://img.shields.io/badge/licen%C3%A7a-BSL%201.1-orange?style=for-the-badge)](LICENSE)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green?style=for-the-badge&logo=android)](https://developer.android.com/studio)

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.x-blue?style=flat-square)](https://developer.android.com/compose)
[![Hilt](https://img.shields.io/badge/Hilt-2.51.x-orange?style=flat-square)](https://dagger.dev/hilt)
[![UI Performance: 120FPS](https://img.shields.io/badge/Performance-120FPS-brightgreen?style=flat-square)](https://github.com/walbarellos/FalaComigo)
[![Security: VirusTotal Clean](https://img.shields.io/badge/Seguran%C3%A7a-Auditado-brightgreen?style=flat-square&logo=google-cloud)](https://www.virustotal.com/gui/home/upload)

---

Este arquivo faz parte do projeto **Fala Comigo**.  
Para informações completas sobre o projeto, visite nossa [Wiki Oficial](https://github.com/walbarellos/FalaComigo/wiki).

> **Comunicação Aumentativa e Alternativa (CAA) para todos.**

O **FalaComigo** é um aplicativo Android desenvolvido em Kotlin e Jetpack Compose, focado em acessibilidade e inclusão. Ele permite que pessoas com necessidades complexas de comunicação utilizem símbolos pictográficos para construir frases e se expressar através de voz (TTS).

---

## ✨ Novidades da Versão Beta (v0.4.2)

Esta versão marca um avanço significativo na estabilidade e identidade visual do projeto:

- **🏠 Layout Edge-to-Edge:** Interface agora respeita as barras de sistema (status e navegação).
- **🎨 Paleta de Cores Padronizada:** Todos os componentes seguem o Design System com contraste otimizado.
- **🛠️ Correções Críticas:** Resolvido o crash na inicialização em builds de Release (R8/ProGuard).
- **📁 Gestão de Rotinas:** Interface de criação totalmente funcional e visível.

---

## 🚀 Como Começar

### Instalação Direta
Acesse as [Releases Oficiais](https://github.com/walbarellos/FalaComigo/releases) e baixe o arquivo `FalaComigo-v0.4.2-beta.apk` ou utilize a versão na raiz:
- 📥 **[Baixar FalaComigo-v0.4.2-beta.apk](./FalaComigo-v0.4.2-beta.apk)**

---

## 🛠️ Desenvolvimento

### Build Local

```bash
# Clone o repositório
git clone -b beta git@github.com:walbarellos/FalaComigo.git

# Build Release (Requer key.properties configurado)
./gradlew :app:assembleRelease
```

---

## ⚖️ Licença e Termos Legais
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

## 📜 Créditos

Os pictogramas utilizados pertencem ao **ARASAAC**.
- **Propriedade:** Governo de Aragão (Espanha).
- **Autor:** Sergio Palao.
- **Licença:** [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/).
- Mais detalhes em [CREDITS.md](./CREDITS.md).

---

## 📈 Star History

<a href="https://www.star-history.com/?repos=walbarellos%2FFalaComigo&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=walbarellos/FalaComigo&type=date&legend=top-left" />
 </picture>
</a>
