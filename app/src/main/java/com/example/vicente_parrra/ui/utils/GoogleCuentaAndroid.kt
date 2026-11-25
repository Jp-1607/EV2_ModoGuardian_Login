package com.example.vicente_parrra.ui.utils

import android.accounts.AccountManager
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CancellationException

internal class ErrorCuentaGoogle(message: String) : Exception(message)

internal object GoogleCuentaAndroid {
    suspend fun token(contexto: Context, clienteId: String, nonce: String): String {
        val opcion = GetSignInWithGoogleOption.Builder(clienteId).setNonce(nonce).build()
        val solicitud = GetCredentialRequest.Builder().addCredentialOption(opcion).build()
        try {
            val credencial = CredentialManager.create(contexto).getCredential(contexto, solicitud).credential
            if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return GoogleIdTokenCredential.createFrom(credencial.data).idToken
            }
            throw ErrorCuentaGoogle("Android no devolvió una cuenta de Google.")
        } catch (_: GetCredentialCancellationException) {
            throw CancellationException("Se canceló el selector de Google")
        } catch (_: NoCredentialException) {
            throw ErrorCuentaGoogle("Agrega una cuenta de Google al dispositivo y vuelve a intentar.")
        } catch (_: GetCredentialException) {
            throw ErrorCuentaGoogle("Google no pudo abrir el selector. Revisa la cuenta y los servicios de Google Play.")
        } catch (_: GoogleIdTokenParsingException) {
            throw ErrorCuentaGoogle("Google devolvió una credencial que la app no pudo leer.")
        }
    }

    suspend fun cerrar(contexto: Context) {
        try {
            CredentialManager.create(contexto).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: ClearCredentialException) {
            Unit
        }
    }

    fun selectorDemo() = AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null)

    fun seudonimoDemo(contexto: Context, nombre: String): String {
        val preferencias = contexto.getSharedPreferences("google_demo", Context.MODE_PRIVATE)
        val semilla = preferencias.getString("instalacion", null) ?: UUID.randomUUID().toString().also {
            preferencias.edit().putString("instalacion", it).apply()
        }
        return MessageDigest.getInstance("SHA-256").digest("$semilla:$nombre".toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
