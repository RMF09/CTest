package com.rmf.ctest.feature.forgot_password

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.rmf.ctest.ui.component.ErrorDialog
import com.rmf.ctest.ui.component.LoadingDialog
import com.rmf.ctest.ui.component.SuccessDialog

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ForgotPasswordScreen(
    navigator: DestinationsNavigator,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.state

    Box {

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(text = "Lupa Password") }, navigationIcon = {
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

                TextField(
                    value = state.email,
                    onValueChange = { value ->
                        viewModel.onChangeEmail(value)
                    },
                    label = {
                        Text(text = "Masukan Email")
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = viewModel::send,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Kirim")
                }
            }
        }

        if (state.isLoading)
            LoadingDialog()
        state.errorMessage?.let {
            ErrorDialog(message = it) {
                viewModel.dismissDialog()
            }
        }
        if (state.password.isNotBlank()) {
            SuccessDialog(message = "Password anda adalah : ${state.password}", onDismiss = { }) {
                viewModel.dismissDialog()
            }
        }
    }
}