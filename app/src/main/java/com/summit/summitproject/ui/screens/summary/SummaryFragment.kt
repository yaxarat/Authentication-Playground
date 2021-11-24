package com.summit.summitproject.ui.screens.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.summit.summitproject.cryptography.CiphertextWrapper

class SummaryFragment : Fragment() {

    private val viewModel: SummaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            val ciphertextWrapper: CiphertextWrapper? = viewModel.state.value.encryptedToken

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        viewModel.showBiometricPromptForEncryption(requireActivity())
                    },
                    enabled = true,
                    modifier = Modifier
                        .padding(all = 32.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Enroll in biometric login",
                        style = MaterialTheme.typography.button
                    )
                }

                if (ciphertextWrapper != null) {
                    Text(text = "Enrollment success!" +
                            "\nIV: ${ ciphertextWrapper.initializationVector }" +
                            "\nEncrypted auth token: ${ ciphertextWrapper.ciphertext }")
                }
            }
        }
    }
}