package com.example.greenlytics.ui.input

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter // Mengimpor fragment dari folder category
import com.example.greenlytics.ui.input.category.ElectricFragment
import com.example.greenlytics.ui.input.category.ShoppingFragment
import com.example.greenlytics.ui.input.category.TransportFragment
import com.example.greenlytics.ui.input.category.WasteFragment

class InputPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Jumlah tab yang akan ditampilkan (Transportasi, Listrik, Belanja, Sampah)
    override fun getItemCount(): Int = 4

    // Mengatur halaman mana yang keluar berdasarkan urutan tab
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TransportFragment() // Tab Pertama
            1 -> ElectricFragment()  // Tab Kedua
            2 -> ShoppingFragment()  // Tab Ketiga
            3 -> WasteFragment()     // Tab Keempat
            else -> TransportFragment()
        }
    }
}