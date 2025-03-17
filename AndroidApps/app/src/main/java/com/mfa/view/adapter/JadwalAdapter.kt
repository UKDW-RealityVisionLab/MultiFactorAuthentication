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
import java.text.SimpleDateFormat
import java.util.Locale

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
                sendData.putExtra(PertemuanActivity.DOSEN,pertemuan.dosen)
                sendData.putExtra(PertemuanActivity.NAMAPERTEMUAN,"${pertemuan.matakuliah} grup ${pertemuan.grup}")
                holder.itemView.context.startActivity(sendData)
            }
        }
    }

    fun submitSortedList(list: List<HomeResponseItem?>?) {
        val daysOrder = mapOf(
            "Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4,
            "Fri" to 5, "Sat" to 6, "Sun" to 7
        )

        val sortedList = list?.sortedBy { item ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

                val date = inputFormat.parse(item?.tanggal)
                val dayAbbreviation = outputFormat.format(date!!) // Misalnya "Mon", "Tue"
                daysOrder[dayAbbreviation] ?: Int.MAX_VALUE // Jika hari tidak ditemukan, beri nilai besar
            } catch (e: Exception) {
                e.printStackTrace()
                Int.MAX_VALUE // Jika terjadi error, masukkan di urutan terakhir
            }
        }

        submitList(sortedList) // Set daftar yang sudah diurutkan ke adapter
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
            binding.grupMatkul.text = "grup ${review.grup}"
            binding.ruanganMatkul.text = review.dosen
            binding.jadwalMatkul.text = "${review.sesiStart} - ${review.sesiEnd}"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) // Sesuaikan dengan format data
            val outputFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

            try {
                val date = inputFormat.parse(review.tanggal) // Konversi String ke Date
                val current = outputFormat.format(date!!) // Format ulang Date ke format yang diinginkan
                binding.hariMatkul.text = current
            } catch (e: Exception) {
                e.printStackTrace()
                binding.hariMatkul.text = review.tanggal // Jika error, tampilkan data asli
            }
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