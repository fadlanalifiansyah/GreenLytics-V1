package com.example.greenlytics.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.greenlytics.R
import com.example.greenlytics.databinding.FragmentProfilBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfilFragment : Fragment() {

    private val viewModel: ProfilViewModel by viewModels()
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    // Menggunakan SharedPreferences untuk menyimpan settingan lokal
    private val sharedPrefs by lazy {
        requireContext().getSharedPreferences("GreenLyticsPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadProfilData()
        loadSettingsData() // Muat settingan lokal (Target & Switch)

        observeViewModel()
        setupListeners()
    }

    private fun loadSettingsData() {
        // Muat Status Switch Notifikasi
        val isNotifOn = sharedPrefs.getBoolean("NOTIF_ON", true)
        binding.switchNotif.isChecked = isNotifOn

        // Muat Target Emisi
        val targetEmisi = sharedPrefs.getString("TARGET_EMISI", "8")
        binding.tvTargetValue.text = "$targetEmisi kg CO₂ per hari"
    }

    private fun setupListeners() {
        // 1. SWITCH NOTIFIKASI
        binding.switchNotif.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("NOTIF_ON", isChecked).apply()
            val msg = if (isChecked) "Notifikasi diaktifkan" else "Notifikasi dimatikan"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // 2. KLIK TARGET EMISI
        binding.btnTargetEmisi.setOnClickListener {
            showTargetDialog()
        }

        // 3. KLIK TENTANG GREENLYTICS
        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }

        // 4. KLIK HUBUNGI KAMI
        binding.btnContact.setOnClickListener {
            openContactEmail()
        }

        // 5. TOMBOL LOGOUT
        binding.btnLogout.setOnClickListener {
            // Langkah 1: Hapus sesi di Firebase Auth
            FirebaseAuth.getInstance().signOut()

            // Langkah 2: Atur aturan pindah halaman (Clear History)
            val navOptions = NavOptions.Builder()
                .setPopUpTo(findNavController().graph.startDestinationId, true)
                .build()

            // Langkah 3: Pindah ke LoginFragment
            try {
                findNavController().navigate(R.id.loginFragment, null, navOptions)
                Toast.makeText(requireContext(), "Berhasil Keluar", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal pindah halaman: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTargetDialog() {
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = "Contoh: 10.5"

        // Ambil data lama untuk ditaruh di kolom input
        val oldTarget = sharedPrefs.getString("TARGET_EMISI", "8")
        editText.setText(oldTarget)

        // Memberikan margin agar tampilan EditText rapi di dalam dialog
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = 56
        params.rightMargin = 56
        editText.layoutParams = params
        container.addView(editText)

        AlertDialog.Builder(requireContext())
            .setTitle("Target Emisi Harian")
            .setMessage("Masukkan batas maksimal emisi harianmu (kg CO₂):")
            .setView(container)
            .setPositiveButton("Simpan") { _, _ ->
                val newValue = editText.text.toString()
                if (newValue.isNotEmpty()) {
                    sharedPrefs.edit().putString("TARGET_EMISI", newValue).apply()
                    binding.tvTargetValue.text = "$newValue kg CO₂ per hari"
                    Toast.makeText(requireContext(), "Target diperbarui!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang GreenLytics")
            .setMessage("GreenLytics adalah aplikasi pelacak jejak karbon harian.\n\nVersi: 1.0.0\nDikembangkan untuk membantu pengguna lebih peduli terhadap lingkungan dengan melacak emisi dari transportasi, listrik, dan limbah.")
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun openContactEmail() {
        // 1. Siapkan subjek dan ubah formatnya agar aman untuk link (Uri.encode)
        val subject = Uri.encode("Bantuan / Masukan Aplikasi GreenLytics")

        // 2. Gabungkan email dan subjek langsung di dalam data URI
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:greenlyticsdeveloper@gmail.com?subject=$subject")
        }

        // 3. Eksekusi
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal membuka email. Pastikan aplikasi Gmail terinstal.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        // 1. Observasi Identitas User
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvProfileName.text = name
            // Set huruf awal dinamis untuk avatar
            if (name.isNotEmpty()) {
                binding.tvInitialAvatar.text = name.take(1).uppercase()
            }
        }

        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.tvProfileEmail.text = email
        }

        // 2. Observasi Statistik Emisi
        viewModel.monthlyCarbon.observe(viewLifecycleOwner) { total ->
            binding.tvTotalEmisi.text = String.format("%.2f kg CO₂", total ?: 0.0)
        }

        viewModel.lowestEmission.observe(viewLifecycleOwner) { lowest ->
            if (lowest != null && lowest > 0) {
                binding.tvEmisiTerbaik.text = String.format("%.2f kg CO₂", lowest)
            } else {
                binding.tvEmisiTerbaik.text = "Belum ada data"
            }
        }

        viewModel.totalActivities.observe(viewLifecycleOwner) { count ->
            binding.tvTotalAktivitas.text = "${count ?: 0} aktivitas"
        }

        // 3. Observasi Streak dan Badge
        viewModel.streakCount.observe(viewLifecycleOwner) { streak ->
            binding.tvStreakCount.text = streak.toString()
        }

        viewModel.badgeCount.observe(viewLifecycleOwner) { badges ->
            binding.tvBadgeCount.text = badges.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}