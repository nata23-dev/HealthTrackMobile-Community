package com.example.healthtrackmobile.service

import com.example.healthtrackmobile.model.Usuario
import com.example.healthtrackmobile.util.PasswordUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthService {
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: CharSequence): Result<Usuario> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email y contraseña no pueden estar vacíos"))
        }

        return try {
            val snapshot = db.collection("usuarios")
                .whereEqualTo("correo", email.trim())
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Usuario no encontrado o contraseña incorrecta"))
            }

            val document = snapshot.documents.first()
            val usuario = document.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Error al procesar los datos del usuario"))

            // Validar contraseña
            if (!PasswordUtils.verifyPassword(password.toString(), usuario.password)) {
                return Result.failure(Exception("Usuario no encontrado o contraseña incorrecta"))
            }

            // Validar que la cuenta esté activa
            if (!usuario.activo) {
                return Result.failure(Exception("CUENTA_INACTIVA"))
            }

            usuario.id = document.id
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun existeCorreo(email: String): Boolean {
        if (email.isBlank()) return false
        return try {
            val snapshot = db.collection("usuarios")
                .whereEqualTo("correo", email.trim())
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}
