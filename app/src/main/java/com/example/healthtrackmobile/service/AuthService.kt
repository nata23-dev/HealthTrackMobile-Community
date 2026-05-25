package com.example.healthtrackmobile.service

import com.example.healthtrackmobile.model.Usuario
import com.example.healthtrackmobile.util.PasswordUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthService {
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: CharSequence): Result<Usuario> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (email.isBlank() || password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email y contraseña no pueden estar vacíos"))
        }

        return@withContext try {
            val snapshot = db.collection("usuarios")
                .whereEqualTo("correo", email.trim())
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return@withContext Result.failure(Exception("Usuario no encontrado o contraseña incorrecta"))
            }

            val document = snapshot.documents.first()
            val usuario = document.toObject(Usuario::class.java)
                ?: return@withContext Result.failure(Exception("Error al procesar los datos del usuario"))

            // Validar contraseña
            if (!PasswordUtils.verifyPassword(password.toString(), usuario.password)) {
                return@withContext Result.failure(Exception("Usuario no encontrado o contraseña incorrecta"))
            }

            // Validar que la cuenta esté activa
            if (!usuario.activo) {
                return@withContext Result.failure(Exception("CUENTA_INACTIVA"))
            }

            usuario.id = document.id
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(nombre: String, email: String, passwordRaw: String): Result<Usuario> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (nombre.isBlank() || email.isBlank() || passwordRaw.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Todos los campos son obligatorios"))
        }

        return@withContext try {
            if (existeCorreo(email)) {
                return@withContext Result.failure(Exception("Este correo ya está registrado en HealthTrack."))
            }

            val hashedPassword = PasswordUtils.hashPassword(passwordRaw)
                ?: return@withContext Result.failure(Exception("Error al cifrar la contraseña"))

            val nuevoUsuario = Usuario(
                nombre = nombre.trim(),
                correo = email.trim(),
                password = hashedPassword,
                rol = "paciente",
                activo = true,
                fechaRegistro = System.currentTimeMillis()
            )

            val docRef = db.collection("usuarios").document()
            val userId = docRef.id
            nuevoUsuario.id = userId
            docRef.set(nuevoUsuario).await()

            Result.success(nuevoUsuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun existeCorreo(email: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (email.isBlank()) return@withContext false
        return@withContext try {
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
