# Release Local - FalaComigo

Este arquivo descreve o estado de release do workspace atual. Ele substitui hashes antigos por evidencias geradas do artefato local.

## Estado

- `:app:assembleDebug`: PASS
- `:app:testDebugUnitTest`: PASS
- `:app:assembleRelease`: PASS
- Debug APK: assinado com chave debug e verificavel.
- Release APK: gerado como unsigned quando nao ha `key.properties`.
- Distribuicao: BLOQUEADA ate existir release assinado com keystore local real.

## Artefatos

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Comandos de Validacao

```bash
./gradlew :app:compileDebugKotlin --no-daemon
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:assembleRelease --no-daemon
scripts/verify_apk_integrity.sh app/build/outputs/apk/debug/app-debug.apk
scripts/verify_apk_integrity.sh app/build/outputs/apk/release/app-release-unsigned.apk
```

## Gate de Assinatura

O release so passa para distribuicao quando:

```text
key.properties existe apenas localmente
keystore real existe fora do Git
assembleRelease gera APK assinado
scripts/verify_apk_integrity.sh retorna SIGNATURE: PASS
SHA-256 e gerado do APK assinado real
```

Enquanto o arquivo gerado for `app-release-unsigned.apk`, ele serve apenas para validar build/R8.

## Gate de Segredos e Logs

Status atual:

```text
sem senha hardcoded em Gradle
sem key.properties no repositorio
sem keystore no repositorio
sem HttpLoggingInterceptor.Level.BODY
sem println/System.out/Log.* em app/src/main/java/br/com/falacomigo
```
