package com.summit.summitproject.biometric

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricPromptUtils {
    private val TAG = this::class.simpleName

    fun createBiometricPrompt(
        fragmentActivity: FragmentActivity,
        onAuthenticationSuccess: (BiometricPrompt.AuthenticationResult) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(fragmentActivity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                Log.d(TAG, "errCode is $errCode and errString is: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Biometric authentication failed for unknown reason.")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                onAuthenticationSuccess(result)
            }
        }
        return BiometricPrompt(fragmentActivity, executor, callback)
    }

    fun createPromptInfo(
        title: String = "Title",
        subtitle: String = "Subtitle",
        description: String = "Description",
        confirmationRequired: Boolean = false,
        negativeButtonText: String = "Cancel"
    ): BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder().apply {
        setTitle(title)
        setSubtitle(subtitle)
        setDescription(description)
        setConfirmationRequired(confirmationRequired)
        setNegativeButtonText(negativeButtonText)
    }.build()
}