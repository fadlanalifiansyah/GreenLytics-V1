package com.example.greenlytics.ui.input

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.greenlytics.ui.input.category.ElectricFragment
import com.example.greenlytics.ui.input.category.TransportFragment
import com.example.greenlytics.ui.input.category.WasteFragment

class InputPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // UBAH JADI 3
    override fun getItemCount(): Int = 3

    // Mengatur halaman mana yang keluar berdasarkan urutan tab
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TransportFragment()
            1 -> ElectricFragment()
            2 -> WasteFragment()
            else -> TransportFragment()
        }
    }
}