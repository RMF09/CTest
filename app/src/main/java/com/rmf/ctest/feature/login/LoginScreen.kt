package com.rmf.ctest.feature.login

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.auth
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.rmf.ctest.R
import com.rmf.ctest.core.data.clientID
import com.rmf.ctest.core.domain.model.Profile
import com.rmf.ctest.feature.NavGraphs
import com.rmf.ctest.feature.destinations.CameraScreenDestination
import com.rmf.ctest.feature.destinations.ForgotPasswordScreenDestination
import com.rmf.ctest.feature.destinations.HomeScreenDestination
import com.rmf.ctest.feature.destinations.RegisterScreenDestination
import com.rmf.ctest.feature.login.LoginWith.EMAIL
import com.rmf.ctest.feature.login.LoginWith.FACE
import com.rmf.ctest.feature.login.LoginWith.FACEBOOK
import com.rmf.ctest.feature.login.LoginWith.GOOGLE
import com.rmf.ctest.rememberFirebaseAuthLauncher
import com.rmf.ctest.ui.component.ErrorDialog
import com.rmf.ctest.ui.component.LoadingDialog
import com.rmf.ctest.util.exhaustive
import com.rmf.ctest.util.toBitmap
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RootNavGraph(start = true)
@Destination
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<CameraScreenDestination, Uri>,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val currentEmailLogin = viewModel.sessionManager.getEmail()
    val launcher =
        rememberFirebaseAuthLauncher(
            onAuthComplete = { result ->
                viewModel.login(
                    GOOGLE,
                    profile = Profile(
                        email = result.user!!.email!!,
                        name = result.user!!.displayName!!
                    )
                )
            },
            onAuthError = {
            }
        )

    val context = LocalContext.current

    resultRecipient.onNavResult { result ->
        when (result) {
            NavResult.Canceled -> {}
            is NavResult.Value -> {
                viewModel.verifyFace(result.value.toBitmap(context))
            }
        }.exhaustive
    }

    //Facebook Staff
    val scope = rememberCoroutineScope()
    val loginManager = LoginManager.getInstance()
    val callbackManager = remember { CallbackManager.Factory.create() }
    val launcherFB = rememberLauncherForActivityResult(
        loginManager.createLogInActivityResultContract(callbackManager, null)
    ) {
        Log.e("TAG", "Login FB callback ", )
    }

    DisposableEffect(Unit) {
        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.e("TAG", "onCancel: Fb " )
                }

                override fun onError(error: FacebookException) {
                    Log.e("TAG", "onError: ${error.message}")
                }

                override fun onSuccess(result: LoginResult) {
                    // user signed in successfully
                    // TODO Forward to Firebase Auth
                    // check next step in composables.com/blog/firebase-auth-facebook
                    scope.launch {
                        val token = result.accessToken.token
                        val credential = FacebookAuthProvider.getCredential(token)
                        val authResult =
                            Firebase.auth.signInWithCredential(credential).await()
                        if (authResult.user != null) {
                            val profile = Profile(
                                email = authResult.user!!.email!!,
                                name = authResult.user!!.displayName!!
                            )
                            viewModel.login(FACEBOOK, profile)

                        } else {
                            Log.e("TAG", "onError:")
                        }
                    }
                }
            })

        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }

    LaunchedEffect(key1 = viewModel.shouldNavigateToHome, key2 = currentEmailLogin) {
        if (viewModel.shouldNavigateToHome || !currentEmailLogin.isNullOrEmpty())
            navigator.navigate(HomeScreenDestination) {
                launchSingleTop = true
                popUpTo(NavGraphs.root)
            }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selamat Datang",
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
            FormLogin(
                state = state,
                onChangeEmail = viewModel::onChangeEmail,
                onChangePassword = viewModel::onChangePassword,
                onClickLogin = { loginWith ->
                    when (loginWith) {
                        EMAIL -> {
                            viewModel.login(EMAIL)
                        }

                        GOOGLE -> {
                            val gso =
                                Builder(DEFAULT_SIGN_IN)
                                    .requestIdToken(clientID)
                                    .requestEmail()
                                    .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            launcher.launch(googleSignInClient.signInIntent)
                        }

                        FACEBOOK -> {
                            launcherFB.launch(listOf("email", "public_profile"))
                        }

                        FACE -> {
                            if (viewModel.validate(isForFace = true))
                                navigator.navigate(CameraScreenDestination())
                            else {
                                // TODO: Do nothing
                            }
                        }
                    }.exhaustive
                },
                onClickForgetPassword = {
                    navigator.navigate(ForgotPasswordScreenDestination)
                }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Belum punya akun? ")
                Text(
                    text = "Daftar",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navigator.navigate(RegisterScreenDestination)
                    }
                )

            }
        }

        if (state.isLoading)
            LoadingDialog()
        state.errorMessage?.let {
            ErrorDialog(message = it) {
                viewModel.dismissDialog()
            }
        }
    }
}

@Composable
fun GoogleAndFbAuth(onClickAuth: (LoginWith) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Atau login dengan :")
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ButtonAuth(logo = R.drawable.google) {
                onClickAuth(GOOGLE)
            }



            ButtonAuth(logo = R.drawable.fb) {
                onClickAuth(FACEBOOK)
            }
            ButtonAuth(logo = R.drawable.baseline_face_24) {
                onClickAuth(FACE)
            }
        }
    }
}

@Composable
fun ButtonAuth(logo: Int, onClick: () -> Unit) {
    Box(modifier = Modifier
        .clip(CircleShape)
        .clickable {
            onClick()
        }
    ) {
        Image(
            painter = painterResource(id = logo),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun FormLogin(
    state: LoginState,
    onChangeEmail: (String) -> Unit,
    onChangePassword: (String) -> Unit,
    onClickLogin: (LoginWith) -> Unit,
    onClickForgetPassword: () -> Unit,
) {
    val focusRequest = LocalFocusManager.current
    var shouldPasswordVisible by remember {
        mutableStateOf(false)
    }

    val visualPassword =
        if (!shouldPasswordVisible) PasswordVisualTransformation()
        else
            VisualTransformation.None

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = state.email,
            onValueChange = onChangeEmail,
            label = {
                Text(text = "Email")
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(onNext = { focusRequest.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = state.password,
            onValueChange = onChangePassword,
            label = {
                Text(text = "Password")
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            maxLines = 1,
            keyboardActions = KeyboardActions(onDone = { focusRequest.clearFocus() }),
            visualTransformation = visualPassword,
            trailingIcon = {
                IconButton(
                    onClick = { shouldPasswordVisible = !shouldPasswordVisible },
                ) {
                    val icon =
                        if (shouldPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inversePrimary
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lupa Password?",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                onClickForgetPassword()
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onClickLogin(EMAIL) }) {
            Text(text = "Login")
        }
        Spacer(modifier = Modifier.height(24.dp))
        GoogleAndFbAuth(
            onClickAuth = { loginWith -> onClickLogin(loginWith) })

    }
}