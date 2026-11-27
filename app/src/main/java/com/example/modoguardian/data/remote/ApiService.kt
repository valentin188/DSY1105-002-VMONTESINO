package com.example.modoguardian.data.remote

import com.example.modoguardian.model.Event
import retrofit2.http.GET

interface ApiService {
    @GET("events")
    suspend fun getEvents(): List<Event>
}
