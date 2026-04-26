# Fala Comigo - Notas da Versão 1.0.0 (MVP)

## 📅 Data de Lançamento
25 de Abril de 2025

---

## 🏷️ Informações da Build

| Item | Valor |
|------|-------|
| **Versão** | 1.0.0 |
| **Código** | 1 |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |
| **Tamanho APK** | ~7.4 MB |
| **Arquitetura** | armeabi-v7a, arm64-v8a, x86, x86_64 |

---

## ✅ O que está incluído (MVP)

### Funcionalidades Principais

1. **Sistema de Símbolos**
   - Grid 4x4 com símbolos pictóricos ARASAAC
   - Cores pastéis para calmaria visual
   - Bordas arredondadas (24dp)
   - Imagens em drawable-nodpi

2. **Navegação por Abas**
   - **Início**: Grid de comunicação básica
   - **Rotinas**: Criar/editar/excluir frases personalizadas
   - **Favoritos**: Top 16 frases mais usadas

3. **Controle de Voz**
   - Síntese TTS nativa do Android
   - Velocidades: Lento, Normal, Rápido

4. **Interface Minimalista**
   - Design calmo (cores pastéis)
   - TabRow no topo
   - Botão de configurações ⚙️

---

## 📋 Novas Implementações desde Beta

### Versão Beta → MVP (v1.0.0)

| # | Mudança | Tipo |
|---|--------|------|
| 1 | Adicionadas 3 abas completo | Novo |
| 2 | Sistema de Favoritos automático | Novo |
| 3 | Criação de Rotinas com FAB (+) | Novo |
| 4 | Edição de Rotinas (lápis) | Novo |
| 5 | Exclusão de Rotinas (lixeira) | Novo |
| 6 | Contador de uso em Favoritos | Novo |
| 7 | Controle de velocidade voz | Novo |
| 8 | .gitignore completo | Novo |
| 9 | README ~900 linhas | Novo |
| 10 | Licença CC BY-NC-ND 4.0 | Novo |

---

## 🧪 Testado em

| Dispositivo | Android | Resultado |
|-------------|---------|------------|
| Emulator (Pixel 7) | 14 | ✅ OK |
| Emulator (Pixel 4) | 13 | ✅ OK |
| Emulator (generic) | 7.0 | ✅ OK |

---

## ⚠️ Limitações Conhecidas

1. **TTS com delay**: Voz pode demorar 5-10s para iniciar (inicialização assíncrona)
2. **Sem persistência**: Dados são perdidos ao desinstalar
3. **Sem backup**: Ainda não suporta sincronização na nuvem
4. **Sem customização de símbolos**: Versão futura

---

## 📦 Arquivos de Distribuição

| Arquivo | Descrição |
|---------|------------|
| `FalaComigo-release.apk` | APK principal (assinado) |
| `FalaComigo-v1.0.0-release.apk.sha256` | Checksum SHA256 |

### Checksum SHA256
```
bc866c7ac55395951079e2ea3bd8a81b7ae901cb09439442433b58fbb09136c5
```

---

## 🔐 Assinatura Digital

| Campo | Valor |
|-------|-------|
| **CN** | Willian Albarello, O=Fala Comigo |
| **Validade** | 10.000 dias |
| **Algoritmo** | SHA256withRSA, 2048-bit |

---

## 📲 Como Instalar

1. **Baixe** o APK `FalaComigo-release.apk`
2. **Habilite** "Fontes Desconhecidas" em Configurações → Segurança
3. **Toque** no APK para iniciar instalação
4. **Abra** o aplicativo da tela inicial

---

## 🐛 Reportando Problemas

Para reportar bugs ou solicitar funcionalidades:

1. Acesse: https://github.com/walbarellos/FalaComigo/issues
2. Clique em "New issue"
3. Selecione o template apropriado

---

## 📄 Licença

Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)

Veja o arquivo LICENSE para detalhes completos.

---

## 💻 Código Fonte

O código fonte está disponível em:
https://github.com/walbarellos/FalaComigo

---

## ⭐ Agradecimentos

- Willian Albarello (desenvolvedor)
- Base de símbolos ARASAAC
- Comunidade open source

---

*Fala Comigo - Democratizando a comunicação para todos.*