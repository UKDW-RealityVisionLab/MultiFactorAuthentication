package com.mfa.view.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mfa.api.response.DataJadwalItem
import com.mfa.api.response.PertemuanResponseItem
import com.mfa.databinding.ItemPertemuanBinding
import com.mfa.view.activity.PertemuanActivity
import com.mfa.view.activity.PresensiActivity

class PertemuanAdapter :
    ListAdapter<PertemuanResponseItem, PertemuanAdapter.PertemuanViewHolder>(
        DIFF_CALLBACK
    ) {

    var isvalid : Boolean? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PertemuanViewHolder {
        val binding = ItemPertemuanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PertemuanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PertemuanViewHolder, position: Int) {
        val pertemuan = getItem(position)
        if (pertemuan != null) {
            holder.bind(pertemuan)
            holder.itemView.setOnClickListener {
                val sendData = Intent(holder.itemView.context, PresensiActivity::class.java)
                Log.d("PertemuanAdapter", "Sending jadwal: ${pertemuan.jadwal}, ruang: ${pertemuan.jadwal}")
                sendData.putExtra(PresensiActivity.GETJADWAL, pertemuan.jadwal)
                sendData.putExtra(PresensiActivity.RUANG, pertemuan.ruang)
                sendData.putExtra(PresensiActivity.ISVALID,isvalid)
                holder.itemView.context.startActivity(sendData)
            }
        }
    }

    inner class PertemuanViewHolder(val binding: ItemPertemuanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pertemuan: PertemuanResponseItem) {
            binding.tanggal.text = pertemuan.tanggal
            binding.namaMatkul.text = pertemuan.jadwal
            // binding.ruangKelas.text = pertemuan.ruang // Uncomment this line if you want to display ruang
            binding.jadwalMatkul.text = "${pertemuan.sesiStart} - ${pertemuan.sesiEnd}"
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PertemuanResponseItem>() {
            override fun areItemsTheSame(oldItem: PertemuanResponseItem, newItem: PertemuanResponseItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                newItem: PertemuanResponseItem,
                oldItem: PertemuanResponseItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
