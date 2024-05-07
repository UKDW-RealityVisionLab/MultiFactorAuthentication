package com.mfa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mfa.api.response.DataJadwalItem
import com.mfa.databinding.ItemJadwalBinding

class JadwalAdapter :
    ListAdapter<DataJadwalItem, JadwalAdapter.MyViewHolder>(
        DIFF_CALLBACK
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemJadwalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = getItem(position)
        if (user != null) {
            holder.bind(user)
//            holder.itemView.setOnClickListener {
//                val sendData = Intent(holder.itemView.context, DetailActivity::class.java)
//                sendData.putExtra(DetailActivity.USER, user.id)
//                sendData.putExtra(DetailActivity.NAME, user.name)
//                holder.itemView.context.startActivity(sendData,
//                    ActivityOptionsCompat.makeSceneTransitionAnimation(holder.itemView.context as Activity)
//                        .toBundle()
//                )
//            }
        }

    }

    inner class MyViewHolder(val binding: ItemJadwalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: DataJadwalItem) {
            binding.namaMatkul.text = "${review.namaMatakuliah}"
            binding.grupMatkul.text = review.groupKelas
            binding.ruanganMatkul.text = review.kodeRuang
            binding.jadwalMatkul.text = review.tanggal
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