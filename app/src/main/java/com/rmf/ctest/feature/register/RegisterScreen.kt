package com.rmf.ctest.feature.register

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.auth
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import com.rmf.ctest.R
import com.rmf.ctest.core.data.clientID
import com.rmf.ctest.core.domain.model.Profile
import com.rmf.ctest.feature.NavGraphs
import com.rmf.ctest.feature.destinations.HomeScreenDestination
import com.rmf.ctest.feature.login.LoginWith
import com.rmf.ctest.rememberFirebaseAuthLauncher
import com.rmf.ctest.ui.component.ErrorDialog
import com.rmf.ctest.ui.component.LoadingDialog
import com.rmf.ctest.ui.component.SuccessDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun RegisterScreen(
    navigator: DestinationsNavigator,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state = viewModel.state

    val launcher =
        rememberFirebaseAuthLauncher(
            onAuthComplete = { result ->
                viewModel.login(
                    LoginWith.GOOGLE,
                    profile = Profile(
                        email = result.user!!.email!!,
                        name = result.user!!.displayName!!
                    )
                )
            },
            onAuthError = {
            }
        )

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
                    Log.e("TAG", "onCancel: Fb ")
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
                            viewModel.login(LoginWith.FACEBOOK, profile)

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

    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel.shouldNavigateToHome) {
        if (viewModel.shouldNavigateToHome)
            navigator.navigate(HomeScreenDestination) {
                launchSingleTop = true
                popUpTo(NavGraphs.root)
            }
    }

    Box {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(text = "Daftar Akun") }, navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                })
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val focusRequest = LocalFocusManager.current
                var shouldPasswordVisible by remember {
                    mutableStateOf(false)
                }

                val visualPassword =
                    if (!shouldPasswordVisible) PasswordVisualTransformation()
                    else
                        VisualTransformation.None

                TextField(
                    value = state.name,
                    onValueChange = viewModel::onChangeName,
                    label = {
                        Text(text = "Nama")
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusRequest.moveFocus(
                            FocusDirection.Down
                        )
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = state.email,
                    onValueChange = viewModel::onChangeEmail,
                    label = {
                        Text(text = "Email")
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusRequest.moveFocus(
                            FocusDirection.Down
                        )
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = state.password,
                    onValueChange = viewModel::onChangePassword,
                    label = {
                        Text(text = "Password")
                    },
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.register() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Daftar")
                }
                Spacer(modifier = Modifier.height(8.dp))
                GoogleAndFbAuth(
                    onClickGoogle = {
                        val gso =
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(clientID)
                                .requestEmail()
                                .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    onClickFacebook = {
                        launcherFB.launch(listOf("email", "public_profile"))
                    }
                )
            }
        }
        if (state.isLoading)
            LoadingDialog()
        if (state.isSuccess)
            SuccessDialog(
                message = "Daftar akun baru berhasil dibuat",
                onDismiss = {
                    viewModel.dismissDialog()
                }
            ) {
                viewModel.dismissDialog()
            }

        state.errorMessage?.let { message ->
            ErrorDialog(message = message) {
                viewModel.dismissDialog()
            }
        }

    }
}


@Composable
fun GoogleAndFbAuth(onClickGoogle: () -> Unit, onClickFacebook: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ButtonAuth2(logo = R.drawable.google, name = "Google") {
            onClickGoogle()
        }
        ButtonAuth2(logo = R.drawable.fb, name = "Facebook") {
            onClickFacebook()
        }
    }
}

@Composable
fun ButtonAuth2(logo: Int, name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .defaultMinSize(minHeight = 42.dp)
            .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(25.dp))
            .clip(RoundedCornerShape(25.dp))
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = logo),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
            )
            Text(
                text = "Lanjutkan dengan $name",
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}