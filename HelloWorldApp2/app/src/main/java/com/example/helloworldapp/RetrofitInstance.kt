package com.example.helloworldapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // 1️⃣ Create Retrofit object
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.102:8080/") // Use your Mac IP for real device
            //.baseUrl("http://10.0.2.2:8080/") // Use this if testing on emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 2️⃣ Create API interface
    val api: LawBloxApi by lazy {
        retrofit.create(LawBloxApi::class.java)
    }
}
