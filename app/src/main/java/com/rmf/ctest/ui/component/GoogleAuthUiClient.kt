package com.rmf.ctest.ui.component

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.rmf.ctest.core.data.clientID
import kotlinx.coroutines.tasks.await


class GoogleAuthUiClient(
    private val context: Context
) {
    var credentialManager: CredentialManager = CredentialManager.create(context)

    suspend fun signIn(
        onAuthComplete: (AuthResult) -> Unit,
        onAuthError: (Exception) -> Unit
    ) {

        val googleIdOption =
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientID)
                .build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

        try {
            val result = credentialManager.getCredential(context, request)

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(credential.data)

            val credentialFCM =
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            val authResult = Firebase.auth.signInWithCredential(credentialFCM).await()

            onAuthComplete(authResult)
        } catch (e: Exception) {
            onAuthError(e)
        }
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        Firebase.auth.signOut()
    }
}