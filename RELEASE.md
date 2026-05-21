# Release Local - FalaComigo

Este arquivo descreve o estado de release do workspace atual. Ele substitui hashes antigos por evidencias geradas do artefato local.

## Estado

- `:app:assembleDebug`: PASS
- `:app:testDebugUnitTest`: PASS
- `:app:assembleRelease`: PASS
- Debug APK: assinado com chave debug e verificavel.
- Release APK: v0.4.4-beta assinado e verificavel localmente.
- Distribuicao: pacote interno controlado; Play Store permanece gate futuro.
- SHA-256 atual: `d85f74d73e3ddc0af48a0e1a1f79f31490fa2d186aa4504f3933221a755f2026`

## Artefatos

```text
app/build/outputs/apk/release/app-release.apk
app/build/outputs/apk/release/app-release.apk.sha256
build/reports/falacomigo/build-evidence-release.md
```

## Comandos de Validacao

```bash
./gradlew :app:compileDebugKotlin --no-daemon
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:assembleRelease --no-daemon
scripts/check_release_safety.sh
scripts/verify_apk_integrity.sh app/build/outputs/apk/release/app-release.apk --write-sha256
scripts/write_build_evidence.sh release app/build/outputs/apk/release/app-release.apk
scripts/package_internal_release.sh app/build/outputs/apk/release/app-release.apk
```

Execucao completa do ciclo local + aparelho real:

```bash
scripts/run_release_device_cycle.sh
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

Arquivo `app-release-unsigned.apk`, se existir, serve apenas para validar build/R8 e nao deve ser distribuido.

## Gate de Segredos e Logs

Status atual:

```text
sem senha hardcoded em Gradle
sem key.properties no repositorio
sem keystore no repositorio
sem HttpLoggingInterceptor.Level.BODY
sem println/System.out/Log.* em app/src/main/java/br/com/falacomigo
```

## Gate de Aparelho Real

Instalacao e smoke check devem ser executados apenas com aparelho conectado via ADB:

```bash
scripts/install_release_apk.sh app/build/outputs/apk/release/app-release.apk
scripts/check_installed_package.sh
scripts/device_smoke_check.sh
scripts/write_device_evidence.sh
```

Se houver conflito de assinatura entre debug e release:

```bash
scripts/install_release_apk.sh app/build/outputs/apk/release/app-release.apk --clean-on-signature-mismatch
```
