package com.jschoi.develop.aop_part04_chapter02.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var instance: Retrofit? = null

    fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl("https://run.mocky.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return instance!!
    }
}