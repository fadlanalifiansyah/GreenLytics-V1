package com.example.greenlytics.ui.tips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.greenlytics.R
import com.example.greenlytics.data.model.TipsModel
import com.example.greenlytics.databinding.ItemTipBinding

class TipsAdapter(
    private var tipsList: List<TipsModel>,
    private val onDoneClicked: (TipsModel) -> Unit
) : RecyclerView.Adapter<TipsAdapter.TipsViewHolder>() {

    inner class TipsViewHolder(private val binding: ItemTipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tip: TipsModel) {
            binding.tvTipTitle.text = tip.judul
            binding.tvTipDescription.text = tip.deskripsi
            binding.tvDifficulty.text = tip.difficulty // Mengambil teks "Mudah/Sedang/Sulit"[cite: 1]

            // Mengatur Ikon berdasarkan kategori agar tidak muncul tanda tanya
            val iconRes = when (tip.kategori) {
                "Transportasi" -> android.R.drawable.ic_menu_directions
                "Listrik" -> android.R.drawable.ic_lock_idle_charging
                "Sampah" -> android.R.drawable.ic_menu_delete
                else -> android.R.drawable.ic_menu_help
            }
            binding.ivTipIcon.setImageResource(iconRes)

            // Mengatur warna background badge Kesulitan[cite: 1]
            val badgeDrawable = when (tip.difficulty) {
                "Mudah" -> R.drawable.bg_badge_mudah
                "Sedang" -> R.drawable.bg_badge_mudah // Pastikan kamu sudah buat drawable ini
                "Sulit" -> R.drawable.bg_badge_mudah   // Pastikan kamu sudah buat drawable ini
                else -> R.drawable.bg_badge_mudah
            }
            binding.tvDifficulty.setBackgroundResource(badgeDrawable)

            binding.btnMarkDone.setOnClickListener { onDoneClicked(tip) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipsViewHolder {
        val binding = ItemTipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TipsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipsViewHolder, position: Int) = holder.bind(tipsList[position])

    override fun getItemCount(): Int = tipsList.size

    fun updateData(newList: List<TipsModel>) {
        this.tipsList = newList
        notifyDataSetChanged()
    }
}