package com.jschoi.develop.aop_part04_chapter02.service

import retrofit2.Call
import retrofit2.http.GET

interface  MusicService {
    @GET("v3/e8f4c4fd-84a4-45f7-bf59-eea37b67996b")
    fun listMusics(): Call<MusicDTO>
}