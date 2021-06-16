package com.jschoi.develop.aop_part04_chapter02

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.jschoi.develop.aop_part04_chapter02.databinding.FragmentPlayerBinding
import com.jschoi.develop.aop_part04_chapter02.net.RetrofitClient
import com.jschoi.develop.aop_part04_chapter02.service.MusicDTO
import com.jschoi.develop.aop_part04_chapter02.service.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment(R.layout.fragment_player), View.OnClickListener,
    SeekBar.OnSeekBarChangeListener {

    companion object {
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }

    private var model: PlayerModel = PlayerModel()
    private var playerBinding: FragmentPlayerBinding? = null
    private var player: SimpleExoPlayer? = null
    private lateinit var playListAdapter: PlayListAdapter

    private val updateSeekRunnable = Runnable {
        updateSeek()
    }


    override fun onClick(view: View?) {
        playerBinding?.let { binding ->
            when (view) {
                binding.playlistImageView -> {
                    //  만약에 서버에서 데이터가 다 불러오지 않았을 때 대응.
                    if (model.currentPosition == -1) return@onClick

                    binding.playerViewGroup.isVisible = model.isWatchingPlayListView
                    binding.playListViewGroup.isVisible = model.isWatchingPlayListView.not()

                    model.isWatchingPlayListView = !model.isWatchingPlayListView
                }

                binding.playControlImageView -> {
                    val player = this.player ?: return@onClick
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }

                binding.skipNextImageView -> {
                    val nextMusic = model.nextMusic() ?: return@onClick
                    playMusic(nextMusic)
                }

                binding.skipPrevImageView -> {
                    val prevMusic = model.prevMusic() ?: return@onClick
                    playMusic(prevMusic)
                }
            }
        }
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        player?.seekTo((seekBar.progress * 1000).toLong())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = FragmentPlayerBinding.bind(view)
        playerBinding = viewBinding

        initPlayView(viewBinding)
        initRecyclerView(viewBinding)
        initEventViews(viewBinding)

        getVideoListFromServer()
    }

    private fun initPlayView(viewBinding: FragmentPlayerBinding) {
        context?.let {
            player = SimpleExoPlayer.Builder(it).build()
        }
        viewBinding.playerView.player = player

        player?.addListener(object : Player.EventListener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                if (isPlaying) {
                    viewBinding.playControlImageView.setImageResource(R.drawable.ic_baseline_pause_24)
                } else {
                    viewBinding.playControlImageView.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)

                updateSeek()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                val newIndex = mediaItem?.mediaId ?: return
                model.currentPosition = newIndex.toInt()
                updatePlayerView(model.currentMusicModel())
                playListAdapter.submitList(model.getAdapterModels())
            }
        })
    }

    private fun updateSeek() {
        val player = this.player ?: return
        val duration = if (player.duration >= 0) player.duration else 0
        val position = player.currentPosition

        // UI UPDATE
        updateSeekUi(duration, position)

        val state = player.playbackState

        view?.removeCallbacks(updateSeekRunnable)
        // 재생중이라면
        if (state != Player.STATE_IDLE && state != Player.STATE_ENDED) {
            // 1초에 한번씩 확인
            view?.postDelayed(updateSeekRunnable, 1000)
        }
    }

    private fun updateSeekUi(duration: Long, position: Long) {
        playerBinding?.let {
            it.playerSeekBar.max = (duration / 1000).toInt()
            it.playerSeekBar.progress = (position / 1000).toInt()

            it.playListSeekBar.max = (duration / 1000).toInt()
            it.playListSeekBar.progress = (position / 1000).toInt()

            it.playTimeTextView.text = String.format(
                "%02d:%02d",
                TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS),
                (position / 1000) % 60
            )
            it.totalTimeTextView.text = String.format(
                "%02d:%02d",
                TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS),
                (duration / 1000) % 60
            )
        }
    }

    private fun updatePlayerView(currentMusicModel: MusicModel?) {
        currentMusicModel ?: return

        playerBinding?.let {
            it.trackTextView.text = currentMusicModel.track
            it.artistTextView.text = currentMusicModel.artist
            Glide.with(it.coverImageView.context)
                .load(currentMusicModel.coverUrl)
                .into(it.coverImageView)
        }
    }

    private fun initRecyclerView(viewBinding: FragmentPlayerBinding) {
        playListAdapter = PlayListAdapter {
            // 음악을 재생
            playMusic(it)
        }
        viewBinding.playListRecyclerView.apply {
            adapter = playListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initEventViews(viewBinding: FragmentPlayerBinding) {
        viewBinding.playlistImageView.setOnClickListener(this)          // 재생목록 버튼
        viewBinding.playControlImageView.setOnClickListener(this)       // 재생버튼
        viewBinding.skipNextImageView.setOnClickListener(this)          // 다음 곡 버튼
        viewBinding.skipPrevImageView.setOnClickListener(this)          // 이전 곡 버튼

        viewBinding.playerSeekBar.setOnSeekBarChangeListener(this)
        viewBinding.playListSeekBar.setOnTouchListener { _, _ -> false }
    }

    /**
     * 음악목록 API
     */
    private fun getVideoListFromServer() {
        RetrofitClient.getInstance().create(MusicService::class.java).listMusics().enqueue(object :
            Callback<MusicDTO> {
            override fun onResponse(call: Call<MusicDTO>, response: Response<MusicDTO>) {
                if (response.isSuccessful.not()) return

                response.body()?.let {
                    model = it.mapper()

                    setMusicList(model.getAdapterModels())
                    playListAdapter.submitList(model.getAdapterModels())
                }
            }

            override fun onFailure(call: Call<MusicDTO>, t: Throwable) {
                Log.e("TAG", "ERROR MESSAGE ${t.message}")
            }
        })
    }

    private fun setMusicList(modelList: List<MusicModel>) {
        context?.let {
            player?.addMediaItems(modelList.map { model ->
                MediaItem.Builder()
                    .setMediaId(model.id.toString())
                    .setUri(model.streamUrl)
                    .build()
            })
            player?.prepare()
            player?.play()
        }
    }

    private fun playMusic(musicModel: MusicModel) {
        model.updateCurrentPosition(musicModel)
        player?.seekTo(model.currentPosition, 0)
        player?.play()
    }

    override fun onStop() {
        super.onStop()
        player?.stop()
        view?.removeCallbacks(updateSeekRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()

        playerBinding = null
        player?.release()
        view?.removeCallbacks(updateSeekRunnable)
    }
}