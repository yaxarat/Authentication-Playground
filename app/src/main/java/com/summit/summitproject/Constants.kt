package com.summit.summitproject

import android.content.SharedPreferences
import com.summit.summitproject.model.AccountInfo

// Name to used specify our preference file.
const val SHARED_PREFERENCES_NAME = "app_shared_preference"

/**
 * The keys under which each field will be stored in the [SharedPreferences].
 */

const val PREF_NAME = "name"

const val PREF_CARD_LAST_FOUR = "card_last_4"

const val PREF_TRANSACTIONS = "transactions"

const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"

/**
 * Other constants.
 */

const val FAKE_AUTH_TOKEN = "fake_auth_token"

const val AUTH_TOKEN_SECRET_KEY_NAME = "auth_token_secret_key_name"
