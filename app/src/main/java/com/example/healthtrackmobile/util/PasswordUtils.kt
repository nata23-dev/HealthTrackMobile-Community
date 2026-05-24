package com.example.healthtrackmobile.util

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun hashPassword(password: String?): String? {
        if (password == null) return null
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val hash = pbkdf2(password.toCharArray(), salt) ?: return null

        return "$ITERATIONS:" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash)
    }

    fun verifyPassword(password: String?, storedHash: String?): Boolean {
        if (password == null || storedHash == null) return false

        // Si no tiene delimitador de formato, verificar en texto plano para compatibilidad
        if (!storedHash.contains(":")) {
            return storedHash == password
        }

        return try {
            val parts = storedHash.split(":")
            if (parts.size != 3) return false

            val iterations = parts[0].toInt()
            val salt = Base64.getDecoder().decode(parts[1])
            val hash = Base64.getDecoder().decode(parts[2])

            val testHash = pbkdf2(password.toCharArray(), salt, iterations, hash.size * 8)
            slowEquals(hash, testHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray): ByteArray? {
        return pbkdf2(password, salt, ITERATIONS, KEY_LENGTH)
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray? {
        val spec = PBEKeySpec(password, salt, iterations, keyLength)
        return try {
            val skf = SecretKeyFactory.getInstance(ALGORITHM)
            skf.generateSecret(spec).encoded
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: InvalidKeySpecException) {
            null
        } finally {
            spec.clearPassword()
        }
    }

    private fun slowEquals(a: ByteArray?, b: ByteArray?): Boolean {
        if (a == null || b == null) return false
        var diff = a.size xor b.size
        var i = 0
        while (i < a.size && i < b.size) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
            i++
        }
        return diff == 0
    }
}
