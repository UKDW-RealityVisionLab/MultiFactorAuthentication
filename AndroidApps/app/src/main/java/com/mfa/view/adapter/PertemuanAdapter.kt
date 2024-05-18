package com.mfa.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mfa.api.response.DataJadwalItemPertemuan

import com.mfa.databinding.ItemPertemuanBinding
import com.mfa.view.activity.PertemuanActivity
import com.mfa.view.activity.PresensiActivity

class PertemuanAdapter :
    ListAdapter<DataJadwalItemPertemuan, PertemuanAdapter.PertemuanViewHolder>(
        DIFF_CALLBACK
    ) {

    var isvalid :Boolean? = null


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

                sendData.putExtra(PresensiActivity.ISVALID,isvalid)
                holder.itemView.context.startActivity(sendData)
            }
        }
    }

    inner class PertemuanViewHolder(val binding: ItemPertemuanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pertemuan: DataJadwalItemPertemuan) {
            binding.tanggal.text = pertemuan.tanggal
            binding.namaMatkul.text = pertemuan.kodeJadwal
            binding.jadwalMatkul.text = "${pertemuan.kodeSesi}"
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataJadwalItemPertemuan>() {
            override fun areItemsTheSame(oldItem: DataJadwalItemPertemuan, newItem: DataJadwalItemPertemuan): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: DataJadwalItemPertemuan,
                newItem: DataJadwalItemPertemuan
            ): Boolean {
                return oldItem == newItem
            }
        }
//        const val ISVALID ="isValid"
    }

}
