package com.jschoi.develop.aop_part04_chapter02

import com.jschoi.develop.aop_part04_chapter02.service.MusicDTO
import com.jschoi.develop.aop_part04_chapter02.service.MusicEntity


fun MusicEntity.mapper(id: Long): MusicModel =
    MusicModel(
        id = id,
        streamUrl = streamUrl,
        coverUrl = coverUrl,
        track = track,
        artist = artist
    )

fun MusicDTO.mapper(): PlayerModel =
    PlayerModel(
        playMusicList = musics.mapIndexed { index, entity ->
            entity.mapper(index.toLong())
        }
    )