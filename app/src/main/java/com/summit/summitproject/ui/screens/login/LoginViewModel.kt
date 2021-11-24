package com.summit.summitproject.ui.screens.login

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.summit.summitproject.AUTH_TOKEN_SECRET_KEY_NAME
import com.summit.summitproject.CIPHERTEXT_WRAPPER
import com.summit.summitproject.FAKE_AUTH_TOKEN
import com.summit.summitproject.PREF_CARD_LAST_FOUR
import com.summit.summitproject.PREF_NAME
import com.summit.summitproject.PREF_TRANSACTIONS
import com.summit.summitproject.SHARED_PREFERENCES_NAME
import com.summit.summitproject.biometric.BiometricPromptUtils
import com.summit.summitproject.cryptography.CiphertextWrapper
import com.summit.summitproject.cryptography.getCryptographyManager
import com.summit.summitproject.model.AccountInfo
import com.summit.summitproject.model.ConversionHelper.encodeTransactionsToJson
import com.summit.summitproject.service.LoginResult
import com.summit.summitproject.service.LoginService
import com.summit.summitproject.service.LoginServiceImpl

/**
 * [ViewModel] for the [LoginFragment]
 * This is where the business logic of the Login Fragment, and its screens live.
 */
class LoginViewModel: ViewModel() {
    /**
     * Initialize [LoginService] that provides methods to initiate a login call and listen for its result.
     */
    private val loginService: LoginService = LoginServiceImpl()

    private val cryptographyManager = getCryptographyManager()

    /**
     * Represents the current [LoginState] of our UI. We launch the screen with this initial state.
     * Observed in [LoginFragment] to update composable UIs accordingly.
     */
    val state = mutableStateOf(LoginState())

    /**
     * Grabs the current snapshot of the [LoginState].
     */
    private val currentState get() = state.value

    fun enterUsername(username: String) {
        state.value = currentState.copy(username = username)
        shouldEnableSignInButton()
    }

    fun enterPassword(password: String) {
        state.value = currentState.copy(password = password)
        shouldEnableSignInButton()
    }

    private fun shouldEnableSignInButton() {
        val usernameFilled = currentState.username.isNotEmpty()
        val passwordFilled = currentState.password.isNotEmpty()
        val enableSignIn = (usernameFilled && passwordFilled)

        state.value = currentState.copy(enableSignIn = enableSignIn)
    }

    fun signIn() {
        performSignIn(
            username = currentState.username,
            password = currentState.password
        )

        state.value = currentState.copy(
            enableSignIn = false,
            handlingSignIn = true
        )
    }

    private fun performSignIn(
        username: String,
        password: String
    ) {
        loginService.loginWithCredentials(
            username = username,
            password = password,
            onResultReceived = { result ->
                loginResultReceived(result)
            }
        )
    }

    private fun loginResultReceived(result: LoginResult) {
        if (result is LoginResult.Success) {
            state.value = currentState.copy(accountInfo = result.accountInfo)
        } else {
            state.value = currentState.copy(
                enableSignIn = true,
                handlingSignIn = false
            )
        }
    }

    fun saveAccountInfo(sharedPreferences: SharedPreferences) {
        val accountInfo = currentState.accountInfo ?: return
        val editor = sharedPreferences.edit()

        editor
            .putString(PREF_NAME, accountInfo.name)
            .putString(PREF_CARD_LAST_FOUR, accountInfo.cardLastFour)
            .putString(PREF_TRANSACTIONS, encodeTransactionsToJson(accountInfo.transactions))
            .apply()
    }

    fun getCiphertextWrapperOrNull(fragmentActivity: FragmentActivity) {
        cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            fragmentActivity,
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        ).let {
            state.value = currentState.copy(ciphertextWrapper = it)
        }
    }

    fun showBiometricPromptForDecryption(fragmentActivity: FragmentActivity) {
        state.value.ciphertextWrapper?.let { textWrapper ->
            val canStronglyAuthenticate = BiometricManager.from(fragmentActivity).canAuthenticate(BIOMETRIC_STRONG)

            if (canStronglyAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                val secretKeyAlias = AUTH_TOKEN_SECRET_KEY_NAME
                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    keyName = secretKeyAlias,
                    initializationVector = textWrapper.initializationVector
                )
                val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
                    fragmentActivity = fragmentActivity,
                    onAuthenticationSuccess = ::decryptServerTokenFromStorage
                )
                val promptInfo = BiometricPromptUtils.createPromptInfo()
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        state.value.ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let { cipher ->
                val plaintext = cryptographyManager.decryptData(
                    ciphertext = textWrapper.ciphertext,
                    cipher = cipher
                )

                if (plaintext == FAKE_AUTH_TOKEN) {
                    performSignIn(
                        username = "username",
                        password = "password"
                    )
                } else {
                    loginResultReceived(result = LoginResult.Failure)
                }
            }
        }
    }
}

/**
 * Represents the state that can be changed by the user in [LoginFragment] UI.
 * We start with these initial property values.
 */
data class LoginState(
    val username: String = "",
    val password: String = "",
    val enableSignIn: Boolean = false,
    val handlingSignIn: Boolean = false,
    val accountInfo: AccountInfo? = null,
    val ciphertextWrapper: CiphertextWrapper? = null
)