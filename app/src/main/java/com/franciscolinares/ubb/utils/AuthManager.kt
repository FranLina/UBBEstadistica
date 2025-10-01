package com.franciscolinares.ubb.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun register(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil. Debe tener al menos 6 caracteres."
                        is FirebaseAuthInvalidCredentialsException -> "El correo electrónico no es válido."
                        is FirebaseAuthUserCollisionException -> "Este correo ya está registrado."
                        else -> "Error al registrar al usuario: ${exception?.localizedMessage}"
                    }
                    onResult(false, errorMessage)
                }
            }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
                        is FirebaseAuthInvalidUserException -> "No existe ninguna cuenta con este correo."
                        else -> "Error al iniciar sesión: ${exception?.localizedMessage}"
                    }
                    onResult(false, errorMessage)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}
