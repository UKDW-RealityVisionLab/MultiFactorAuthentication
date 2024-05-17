package com.mfa.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mfa.api.response.DataJadwalItem
import com.mfa.databinding.ItemJadwalBinding
import com.mfa.view.activity.HomeActivity
import com.mfa.view.activity.PertemuanActivity

class JadwalAdapter :
    ListAdapter<DataJadwalItem, JadwalAdapter.MyViewHolder>(
        DIFF_CALLBACK
    ) {
    fun getItemAtPosition(position: Int): DataJadwalItem {
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
        val user = getItem(position)
        if (user != null) {
            holder.bind(user)
            // Set listener onItemClickListener di sini di luar dari itemView.setOnClickListener
            holder.setOnItemClickListener { position ->
                // Handler ketika item diklik dengan onItemClickListener
                val pertemuan = getItem(position)
                val sendData = Intent(holder.itemView.context, PertemuanActivity::class.java)
                sendData.putExtra(PertemuanActivity.RUANG, pertemuan?.kodeRuang )
                sendData.putExtra(PertemuanActivity.KODEJADWAL,pertemuan?.kodeJadwal)
                holder.itemView.context.startActivity(sendData)
            }
        }
    }

    inner class MyViewHolder(val binding: ItemJadwalBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Set OnClickListener untuk itemView
            binding.root.setOnClickListener {
                // Handler ketika item diklik
                val sendData = Intent(binding.root.context, HomeActivity::class.java)
                sendData.putExtra(
                    HomeActivity.TAG,
                    getItem(adapterPosition).kodeKelas
                )
                sendData.putExtra(HomeActivity.NAME, getItem(adapterPosition).namaMatakuliah)
                binding.root.context.startActivity(
                    sendData,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(binding.root.context as Activity)
                        .toBundle()
                )
            }
        }

        fun bind(review: DataJadwalItem) {
            binding.namaMatkul.text = "${review.namaMatakuliah}"
            binding.grupMatkul.text = review.groupKelas
            binding.ruanganMatkul.text = review.kodeRuang
            binding.jadwalMatkul.text = review.tanggal
        }

        fun setOnItemClickListener(listener: (Int) -> Unit) {
            binding.root.setOnClickListener {
                listener(adapterPosition)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataJadwalItem>() {
            override fun areItemsTheSame(oldItem: DataJadwalItem, newItem: DataJadwalItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: DataJadwalItem,
                newItem: DataJadwalItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}