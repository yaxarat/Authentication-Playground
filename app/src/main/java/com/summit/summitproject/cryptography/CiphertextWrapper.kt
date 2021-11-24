package com.summit.summitproject.cryptography

data class CiphertextWrapper(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)
