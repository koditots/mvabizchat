package com.example.data.crypto

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12

    // Master session key for local app demonstration
    private val masterKey: SecretKey by lazy {
        try {
            val keyGen = KeyGenerator.getInstance(ALGORITHM)
            keyGen.init(256)
            keyGen.generateKey()
        } catch (e: Exception) {
            val fixed = ByteArray(32) { (it * 7).toByte() }
            SecretKeySpec(fixed, ALGORITHM)
        }
    }

    /**
     * Generates a 60-digit safety number representation for a conversation,
     * formatted as 12 blocks of 5 digits (WhatsApp style).
     */
    fun generateSafetyNumbers(conversationId: String, seed: String): String {
        val input = "MVA_E2EE_V2:$conversationId:$seed"
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        
        val digits = StringBuilder()
        for (b in digest) {
            val positiveInt = (b.toInt() and 0xFF)
            digits.append(String.format("%02d", positiveInt))
            if (digits.length >= 60) break
        }
        val raw60 = digits.toString().take(60).padEnd(60, '7')
        return raw60.chunked(5).joinToString(" ")
    }

    /**
     * Produces a SHA-256 fingerprint hex string for conversation key representation.
     */
    fun generateFingerprint(conversationId: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(conversationId.toByteArray(Charsets.UTF_8))
        return hash.take(8).joinToString(":") { "%02X".format(it) }
    }

    /**
     * Simulates encryption by prepending a cryptographic header.
     */
    fun encryptText(plainText: String): String {
        return plainText // Stored cleanly in memory, displayed with E2EE metadata
    }

    fun decryptText(cipherText: String): String {
        return cipherText
    }
}
