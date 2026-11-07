package com.rutalista.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val api: OrsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.heigit.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OrsApi::class.java)
    }
}