package com.example.greenlytics.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.greenlytics.databinding.FragmentProfilBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfilFragment : Fragment() {

    private val viewModel: ProfilViewModel by viewModels()

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load data setiap kali fragment dibuka
        viewModel.loadProfilData()

        observeViewModel()
        setupListeners()
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
            binding.tvTotalEmisi.text = String.format("%.2f kg CO2", total ?: 0.0)
        }

        viewModel.lowestEmission.observe(viewLifecycleOwner) { lowest ->
            if (lowest != null && lowest > 0) {
                binding.tvEmisiTerbaik.text = String.format("%.2f kg CO2", lowest)
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

    private fun setupListeners() {
        // Tombol Logout (Bisa disesuaikan nanti dengan fungsi logout Auth)
        binding.btnLogout.setOnClickListener {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            requireActivity().finish() // Tutup activity saat logout
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}