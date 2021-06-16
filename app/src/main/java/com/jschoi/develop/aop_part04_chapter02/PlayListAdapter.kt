package com.jschoi.develop.aop_part04_chapter02

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jschoi.develop.aop_part04_chapter02.databinding.ItemMusicBinding

class PlayListAdapter(private val callback: (MusicModel) -> Unit) :
    ListAdapter<MusicModel, PlayListAdapter.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MusicModel>() {
            override fun areItemsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MusicModel) {
            binding.itemArtistTextView.text = item.artist
            binding.itemTrackTextView.text = item.track

            Glide.with(binding.itemCoverImageView.context)
                .load(item.coverUrl)
                .into(binding.itemCoverImageView)

            if (item.isPlaying) {
                itemView.setBackgroundColor(Color.GRAY)
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            itemView.setOnClickListener {
                callback(item)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        currentList[position].also { musicModel ->
            holder.bind(musicModel)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}