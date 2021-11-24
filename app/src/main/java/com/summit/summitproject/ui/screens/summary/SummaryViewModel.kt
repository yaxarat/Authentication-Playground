package com.summit.summitproject.ui.screens.summary

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.summit.summitproject.AUTH_TOKEN_SECRET_KEY_NAME
import com.summit.summitproject.biometric.BiometricPromptUtils
import com.summit.summitproject.cryptography.CiphertextWrapper
import com.summit.summitproject.cryptography.CryptographyManager
import com.summit.summitproject.cryptography.getCryptographyManager
import com.summit.summitproject.model.Transaction
import com.summit.summitproject.CIPHERTEXT_WRAPPER
import com.summit.summitproject.FAKE_AUTH_TOKEN
import com.summit.summitproject.SHARED_PREFERENCES_NAME

/**
 * [ViewModel] for the [SummaryFragment]
 * This is where the business logic of the Login Fragment, and its screens live.
 */
class SummaryViewModel : ViewModel() {
    private lateinit var cryptographyManager: CryptographyManager

    /**
     * Represents the current [SummaryState] of our UI. We launch the screen with this initial state.
     * Observed in [SummaryFragment] to update composable UIs accordingly.
     */
    val state = mutableStateOf(SummaryState())

    private val currentState get() = state.value

    fun showBiometricPromptForEncryption(fragmentActivity: FragmentActivity) {
        val canStronglyAuthenticate = BiometricManager.from(fragmentActivity).canAuthenticate(BIOMETRIC_STRONG)

        if (canStronglyAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = AUTH_TOKEN_SECRET_KEY_NAME
            cryptographyManager = getCryptographyManager()
            val cipher = cryptographyManager.getInitializedCipherForEncryption(keyName = secretKeyName)
            val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(fragmentActivity = fragmentActivity) { successResult ->
                encryptAndStoreServerToken(
                    result = successResult,
                    context = fragmentActivity
                )
            }
            val promptInfo = BiometricPromptUtils.createPromptInfo()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun encryptAndStoreServerToken(
        result: BiometricPrompt.AuthenticationResult,
        context: Context
    ) {
        result.cryptoObject?.cipher?.apply {
            FAKE_AUTH_TOKEN.let { token ->
                val encryptedToken = cryptographyManager.encryptData(token, this)
                state.value = currentState.copy(encryptedToken = encryptedToken)

                cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                    ciphertextWrapper = encryptedToken,
                    context = context,
                    filename = SHARED_PREFERENCES_NAME,
                    mode = Context.MODE_PRIVATE,
                    sharedPreferencesKey = CIPHERTEXT_WRAPPER
                )
            }
        }
    }
}

data class SummaryState(
    val encryptedToken: CiphertextWrapper? = null
)