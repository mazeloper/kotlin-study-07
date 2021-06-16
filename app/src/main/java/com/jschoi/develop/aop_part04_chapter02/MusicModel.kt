package com.jschoi.develop.aop_part04_chapter02

data class MusicModel(
    val id: Long,
    val track: String,
    val streamUrl: String,
    val artist: String,
    val coverUrl: String,
    val isPlaying: Boolean = false,
)
