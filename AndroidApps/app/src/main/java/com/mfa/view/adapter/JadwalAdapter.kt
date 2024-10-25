package com.mfa.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mfa.api.response.HomeResponseItem
import com.mfa.databinding.ItemJadwalBinding
import com.mfa.view.activity.HomeActivity
import com.mfa.view.activity.PertemuanActivity

class JadwalAdapter :
    ListAdapter<HomeResponseItem, JadwalAdapter.MyViewHolder>(
        DIFF_CALLBACK
    ) {

    fun getItemAtPosition(position: Int): HomeResponseItem? {
        return getItem(position)
    }

    private var onItemClickListener: ((Int) -> Unit)? = null

    // Metode untuk menetapkan listener
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemJadwalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pertemuan = getItem(position)
        if (pertemuan != null) {
            holder.bind(pertemuan)
            holder.setOnItemClickListener {
                val sendData = Intent(holder.itemView.context, PertemuanActivity::class.java)
                sendData.putExtra(PertemuanActivity.KODEKELAS, pertemuan.kodeKelas)
                sendData.putExtra(PertemuanActivity.NAMAPERTEMUAN,pertemuan.matakuliah)
                holder.itemView.context.startActivity(sendData)
            }
        }
    }


    inner class MyViewHolder(val binding: ItemJadwalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // Set OnClickListener untuk itemView
            binding.root.setOnClickListener {
                // Handler ketika item diklik
                val sendData = Intent(binding.root.context, HomeActivity::class.java)
                sendData.putExtra(
                    HomeActivity.TAG,
                    getItem(adapterPosition).kodeKelas
                )
                sendData.putExtra(HomeActivity.NAME, getItem(adapterPosition).matakuliah)
                binding.root.context.startActivity(
                    sendData,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(binding.root.context as Activity)
                        .toBundle()
                )
            }
        }

        fun bind(review: HomeResponseItem) {
            binding.namaMatkul.text = "${review.matakuliah}"
            binding.grupMatkul.text = review.grup
            binding.ruanganMatkul.text = review.dosen
            binding.jadwalMatkul.text = ""
        }

        fun setOnItemClickListener(listener: (Int) -> Unit) {
            binding.root.setOnClickListener {
                listener(adapterPosition)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HomeResponseItem>() {
            override fun areItemsTheSame(
                oldItem: HomeResponseItem,
                newItem: HomeResponseItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: HomeResponseItem,
                newItem: HomeResponseItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}