<img width="592" height="592" alt="image" src="https://github.com/user-attachments/assets/10feb810-b4f5-4bf5-9b40-436bf267f07a" />

## Resumo

Patch de estabilidade da linha Apex, focado em imagem offline, primeira pintura determinística e correção da tela de urgência. Mantém os recursos da v0.4.0 e corrige pontos observados em testes manuais e auditoria técnica.

## Destaques

- **Ícone Colorido**: Nova identidade visual aplicada ao launcher e interface.
- **Sincronização Prancha**: Itens criados em "Meus Itens" agora aparecem automaticamente na "Prancha" principal.
- **Categorias Inteligentes**: Filtros de Lazer, Necessidades e Sentir agora agrupam categorias relacionadas.
- Imagens persistentes em `filesDir`, com `localImagePath` e `thumbnailPath` salvos no Room.
- Bootstrap da primeira tela agora é assíncrono, mantendo a fluidez instantânea do app.
- Tela **Urgente** corrigida para não ficar em carregamento infinito.
- Editor, picker e tela de urgência alinhados ao mesmo resolvedor local-first.
- Migração Room `30 -> 31` sem `fallbackToDestructiveMigration()`.
- Redução de warm-up/haptic desnecessário na primeira composição.
- Cooldown para `warmUpTts()` durante rolagem rápida.
- Testes unitários JVM ajustados com Robolectric.

## Validação

- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`
- `./gradlew assembleRelease`
- Instalação manual via `adb install -r`
- Teste manual de estabilidade: sem erros fatais, sem `ERROR` em logcat, memória saudável e banco ativo.

## Artefatos

- `FalaComigo-v0.4.2-beta.apk`

## SHA-256

`18a4d82b3a253ff1b9a49f050bf9ea1b233cb1cf4e31a5e91c773a9cb3c8b10f`

[**VirusTotal Scan**](https://www.virustotal.com/gui/file/18a4d82b3a253ff1b9a49f050bf9ea1b233cb1cf4e31a5e91c773a9cb3c8b10f/detection)

**Permhash**
_153734b4dd84d3814f04da3569b7a66a_

<img width="1376" height="206" alt="image" src="https://github.com/user-attachments/assets/d1616cf6-cf25-4e29-9546-fa6ad211db9b" />
