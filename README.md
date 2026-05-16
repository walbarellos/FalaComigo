# 📢 FalaComigo Beta v0.4.2

> **Comunicação Aumentativa e Alternativa (CAA) para todos.**

O **FalaComigo** é um aplicativo Android desenvolvido em Kotlin e Jetpack Compose, focado em acessibilidade e inclusão. Ele permite que pessoas com necessidades complexas de comunicação utilizem símbolos pictográficos para construir frases e se expressar através de voz (TTS).

---

## ✨ Novidades da Versão Beta (v0.4.2)

Esta versão marca um avanço significativo na estabilidade e identidade visual do projeto:

- **🏠 Layout Edge-to-Edge:** Interface agora respeita as barras de sistema (status e navegação), garantindo que nada fique escondido sob o relógio ou botões do Android.
- **🎨 Paleta de Cores Padronizada:** Todos os componentes de Gestão, Rotinas e Diálogos de PIN agora seguem estritamente o Design System, com contraste otimizado para visibilidade.
- **🛠️ Correções Críticas:** Resolvido o problema de crash na inicialização em builds de Release causado pela minificação do R8.
- **📁 Gestão de Rotinas:** Interface de criação de palavras e grupos totalmente funcional e visível.

---

## 🚀 Como Começar

### Pré-requisitos
- Dispositivo Android com **Android 7.0 (API 24)** ou superior.
- Mecanismo de **Text-to-Speech (Google TTS)** instalado e atualizado para melhor experiência.

### Instalação Direta
Você pode baixar o APK da última build estável diretamente na raiz deste repositório:
- 📥 **[Baixar FalaComigo-v0.4.2-beta.apk](./FalaComigo-v0.4.2-beta.apk)**

---

## 🛠️ Desenvolvimento

### Tecnologias Utilizadas
- **UI:** Jetpack Compose & Material Design 3
- **Arquitetura:** Clean Architecture com MVVM
- **Injeção de Dependência:** Hilt
- **Banco de Dados:** Room (SQLite)
- **Carregamento de Imagens:** Coil
- **Símbolos:** Integração com o catálogo **ARASAAC**

### Build Local
Para compilar o projeto em seu ambiente:

```bash
# Clone o repositório
git clone -b beta git@github.com:walbarellos/FalaComigo.git

# Build Debug
./gradlew :app:assembleDebug

# Build Release (Requer key.properties configurado)
./gradlew :app:assembleRelease
```

---

## 📜 Licença e Créditos

### Pictogramas
Os pictogramas utilizados são do **ARASAAC**.
- **Propriedade:** Governo de Aragão (Espanha).
- **Autor:** Sergio Palao.
- **Licença:** [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/).
- Mais detalhes em [CREDITS.md](./CREDITS.md).

### Código Fonte
Projeto desenvolvido com foco em impacto social e código aberto.

---

## 🤝 Contribuições
Feedback é essencial nesta fase Beta! Se encontrar bugs ou tiver sugestões de novos símbolos, abra uma *Issue* ou envie um *Pull Request*.

---
*Feito com 💙 para promover a autonomia na comunicação.*
