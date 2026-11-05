package com.example.modoguardian

import com.example.modoguardian.model.Event
import com.example.modoguardian.viewmodel.EventsViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest : StringSpec(body = {
    "debe validar la lista simulada de eventos correctamente" {
        val fakeEvents = listOf(
            Event(id = "1", title = "Alarma Activada", description = "Movimiento detectado"),
            Event(id = "2", title = "Puerta Abierta", description = "Acceso principal")
        )

        val testViewModel = object : EventsViewModel() {
            fun setFakeEvents() {
                _events.value = fakeEvents
            }
        }

        runTest {
            testViewModel.setFakeEvents()
            testViewModel.events.value shouldContainExactly fakeEvents
        }
    }
})
