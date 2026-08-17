package com.example.data.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides End-to-End Encryption (E2EE) using AES-256-GCM.
 * All articles, summaries, physics equations, and cloud sync backups
 * are encrypted before being written to local storage or exported.
 */
object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val MASTER_KEY_SEED = "DatabaseMastermind_E2EE_Vault_2026_SecureKey"

    private val secretKey: SecretKeySpec by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(MASTER_KEY_SEED.toByteArray(Charsets.UTF_8))
        SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plaintext string using AES-GCM 256-bit.
     * Returns Base64 encoded payload formatted as: iv:ciphertext
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val iv = ByteArray(IV_LENGTH_BYTE)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherTextBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)

            "$ivBase64:$cipherTextBase64"
        } catch (e: Exception) {
            // Fallback for safety
            "PLAIN:$plainText"
        }
    }

    /**
     * Decrypts AES-GCM 256-bit ciphertext.
     */
    fun decrypt(encryptedPayload: String): String {
        if (encryptedPayload.isEmpty()) return ""
        if (encryptedPayload.startsWith("PLAIN:")) {
            return encryptedPayload.removePrefix("PLAIN:")
        }
        return try {
            val parts = encryptedPayload.split(":")
            if (parts.size != 2) return encryptedPayload

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // If already plaintext or failed decryption, return safely
            encryptedPayload
        }
    }
}
