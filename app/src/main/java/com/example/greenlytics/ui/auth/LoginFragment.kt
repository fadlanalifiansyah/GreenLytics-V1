package com.example.greenlytics.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.greenlytics.R
import com.example.greenlytics.data.remote.GoogleAuth
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth // <-- TAMBAHKAN IMPORT INI
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Jika user sudah login sebelumnya, langsung lempar ke Dashboard!
            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
            return // Hentikan eksekusi kode di bawahnya agar tidak berbenturan
        }

        setupObservers()

        view.findViewById<Button>(R.id.btn_google_login).setOnClickListener {
            startGoogleSignIn()
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { user ->
                if (user != null) {
                    // MEMUNCULKAN NOTIFIKASI
                    Toast.makeText(requireContext(), "Selamat Datang, ${user.displayName}", Toast.LENGTH_SHORT).show()

                    // Pindah ke Dashboard
                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                }
            }.onFailure { e ->
                // Notifikasi jika gagal
                Toast.makeText(requireContext(), "Login Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startGoogleSignIn() {
        val credentialManager = CredentialManager.create(requireContext())
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GoogleAuth.getGoogleIdOption(requireContext()))
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(requireContext(), request)
                handleSignIn(result)
            } catch (e: Exception) {
                // Notifikasi jika user batal memilih akun
                Toast.makeText(requireContext(), "Batal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)

            // Kirim ke ViewModel
            viewModel.loginWithGoogle(authCredential)
        }
    }
}