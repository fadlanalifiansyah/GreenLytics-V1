package com.example.greenlytics.ui.tips

import android.graphics.Color
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
            binding.tvDifficulty.text = tip.difficulty

            val iconRes = when (tip.kategori) {
                "Transportasi" -> android.R.drawable.ic_menu_directions
                "Listrik" -> android.R.drawable.ic_lock_idle_charging
                "Sampah" -> android.R.drawable.ic_menu_delete
                "Belanja" -> android.R.drawable.ic_menu_gallery // Sesuaikan jika ada ikon keranjang belanja
                else -> android.R.drawable.ic_menu_help
            }
            binding.ivTipIcon.setImageResource(iconRes)

            val badgeDrawable = when (tip.difficulty) {
                "Mudah" -> R.drawable.bg_badge_mudah
                "Sedang" -> R.drawable.bg_badge_mudah
                "Sulit" -> R.drawable.bg_badge_mudah
                else -> R.drawable.bg_badge_mudah
            }
            binding.tvDifficulty.setBackgroundResource(badgeDrawable)

            // LOGIKA WARNA TOMBOL (Berdasarkan state isCompleted)
            if (tip.isCompleted) {
                // State Hijau (Sudah Diklik)
                binding.btnMarkDone.setBackgroundResource(R.drawable.bg_btn_selesai)
                binding.ivCheckIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.primary))
                binding.tvMarkDoneText.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                binding.tvMarkDoneText.text = "Selesai"
                binding.btnMarkDone.isEnabled = false
            } else {
                // State Abu-abu (Belum Diklik)
                binding.btnMarkDone.setBackgroundResource(R.drawable.bg_btn_inactive)
                binding.ivCheckIcon.setColorFilter(Color.parseColor("#9E9E9E"))
                binding.tvMarkDoneText.setTextColor(Color.parseColor("#9E9E9E"))
                binding.tvMarkDoneText.text = "Tandai Selesai"
                binding.btnMarkDone.isEnabled = true
            }

            // Aksi saat ditekan
            binding.btnMarkDone.setOnClickListener {
                tip.isCompleted = true
                notifyItemChanged(adapterPosition) // Memicu ulang logika di atas agar UI berubah
                onDoneClicked(tip)
            }
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