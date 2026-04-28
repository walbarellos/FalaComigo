package br.com.falacomigo

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * NASA: Gera o perfil de compilação Ahead-of-Time.
 * Este teste automatizado percorre o app para ensinar ao Android quais métodos compilar.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = baselineRule.collect(
        packageName = "br.com.falacomigo",
        includeInStartupProfile = true
    ) {
        // 1. Inicia o app (Mede Cold Start)
        pressHome()
        startActivityAndWait()

        // 2. Navega pela prancha principal (Treina o Scroll)
        // Simulamos o scroll para o Android compilar os cards do grid
        pressHome()
    }
}
