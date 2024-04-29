package com.rmf.ctest

import android.content.Intent
import android.os.Bundle
import android.service.credentials.BeginCreateCredentialRequest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.ramcosta.composedestinations.DestinationsNavHost
import com.rmf.ctest.feature.NavGraphs
import com.rmf.ctest.ui.theme.CTestTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this)
        setContent {
            CTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var user by remember { mutableStateOf(Firebase.auth.currentUser) }
                    val launcher =
                        rememberFirebaseAuthLauncher(
                            onAuthComplete = { result ->
                                user = result.user
                            },
                            onAuthError = {
                                user = null
                            }
                        )

                    val context = LocalContext.current
//                    Column {
//                        if (user == null) {
//                            Text(text = "Not Logged in")
//                            Button(onClick = {
//                                val gso =
//                                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                                        .requestIdToken(token)
//                                        .requestEmail()
//                                        .build()
//                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
//                                launcher.launch(googleSignInClient.signInIntent)
//                            }) {
//                                Text(text = "Sign in")
//                            }
//                        } else {
//                            Text("Welcome ${user!!.displayName}")
//                            Button(onClick = {
//                                Firebase.auth.signOut()
//                                user = null
//                            }) {
//                                Text("Sign out")
//                            }
//                        }
//                        FacebookButton(onAuthComplete = {
//                            Log.e("TAG", "onCreate: Berhasil Login Facebook", )
//                        }, onAuthError = {
//                            Log.e("TAG ERROR", "onAuthError:  Login Facebook ${it.message}" )
//                            it.printStackTrace()
//                        })
//                    }
                    DestinationsNavHost(navGraph = NavGraphs.root)
                }
            }
        }
    }
}


@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        } catch (e: ApiException) {
            onAuthError(e)
        }

    }
}
@Composable
fun FacebookButton(
    onAuthComplete: () -> Unit,
    onAuthError: (Exception) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val loginManager = LoginManager.getInstance()
    val callbackManager = remember { CallbackManager.Factory.create() }
    val launcher = rememberLauncherForActivityResult(
        loginManager.createLogInActivityResultContract(callbackManager, null)) {
        // nothing to do. handled in FacebookCallback
    }

    DisposableEffect(Unit) {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {
                // do nothing
            }

            override fun onError(error: FacebookException) {
                onAuthError(error)
            }

            override fun onSuccess(result: LoginResult) {
                // user signed in successfully
                // TODO Forward to Firebase Auth
                // check next step in composables.com/blog/firebase-auth-facebook
                scope.launch {
                    val token = result.accessToken.token
                    val credential = FacebookAuthProvider.getCredential(token)
                    val authResult = Firebase.auth.signInWithCredential(credential).await()
                    if (authResult.user != null) {
                        onAuthComplete()
                    } else {
                        onAuthError(IllegalStateException("Unable to sign in with Facebook"))
                    }
                }
            }
        })

        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }
    Button(
        modifier = modifier,
        onClick = {
            // start the sign-in flow
            launcher.launch(listOf("email", "public_profile"))
        }) {
        Text("Continue with Facebook")
    }
    
}
