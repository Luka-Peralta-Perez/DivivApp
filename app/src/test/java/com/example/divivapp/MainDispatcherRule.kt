package com.example.divivapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Regla JUnit que reemplaza Dispatchers.Main por UnconfinedTestDispatcher
// antes de cada test y lo restaura al terminar.
//
// viewModelScope usa Dispatchers.Main internamente. En tests unitarios el
// Looper de Android no esta disponible, lo que causa IllegalStateException.
// UnconfinedTestDispatcher ejecuta las coroutines inmediatamente en el hilo
// del test, sin necesidad del Looper real de Android.
//
// Uso en tests: @get:Rule val mainDispatcherRule = MainDispatcherRule()
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {

    // Declaro el dispatcher como val en el cuerpo de la clase para que el
    // @OptIn de clase lo cubra correctamente durante la compilacion.
    val testDispatcher = UnconfinedTestDispatcher()

    override fun starting(description: Description) {
        // Reemplazo el dispatcher principal antes de que empiece el test
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        // Restauro el dispatcher original al terminar para no afectar otros tests
        Dispatchers.resetMain()
    }
}
