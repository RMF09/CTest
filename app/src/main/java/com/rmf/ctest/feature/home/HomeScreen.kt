@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.rmf.ctest.feature.home

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.rmf.ctest.core.data.dto.FakturDto
import com.rmf.ctest.feature.NavGraphs
import com.rmf.ctest.feature.destinations.CameraScreenDestination
import com.rmf.ctest.feature.destinations.LoginScreenDestination
import com.rmf.ctest.ui.component.ErrorDialog
import com.rmf.ctest.ui.component.LoadingDialog
import com.rmf.ctest.ui.component.SuccessDialog
import com.rmf.ctest.util.exhaustive
import com.rmf.ctest.util.toBitmap
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<CameraScreenDestination, Uri>,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lazyListState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val state = viewModel.state
    val context = LocalContext.current

    resultRecipient.onNavResult { result ->
        when (result) {
            NavResult.Canceled -> {}
            is NavResult.Value -> {
                viewModel.enrollFace(result.value.toBitmap(context))
            }
        }.exhaustive
    }

    Box {
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    icon = { Icon(imageVector = Icons.Default.Logout, contentDescription = null) },
                    label = { Text(text = "Logout") },
                    selected = false,
                    onClick = {
                        viewModel.logout()
                        navigator.navigate(LoginScreenDestination) {
                            launchSingleTop = true
                            popUpTo(NavGraphs.root)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = "Tentukan Face Authentication") },
                    selected = false,
                    onClick = {
                        navigator.navigate(CameraScreenDestination())
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }) {
            Scaffold(
                topBar = {
                    TopAppBar(title = { /*TODO*/ }, navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                        }
                    })
                }
            ) {
                LazyColumn(
                    state = lazyListState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {
                    item {
                        Text(
                            text = state.email,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    items(state.list) { item ->
                        FakturItem(fakturDto = item)
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    if (state.isLoadingFaktur)
                        item {
                            CircularProgressIndicator()
                        }
                    state.errorMessage?.let { message ->
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = message,
                                )
                                Button(onClick = viewModel::retryFaktur) {
                                    Text(
                                        text = "Retry",
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }

        if (state.isLoading)
            LoadingDialog()
        if (state.isSuccessToEnrollFace)
            SuccessDialog(
                message = "Berhasil menambahkan wajah Anda",
                onDismiss = viewModel::onDismissDialog
            ) {
                viewModel.onDismissDialog()
            }
        state.errorMessage?.let { message ->
            ErrorDialog(message = message) {
                viewModel.onDismissDialog()
            }
        }

    }

}

@Composable
fun FakturItem(fakturDto: FakturDto) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), horizontalAlignment = Alignment.End
        ) {
            Text(text = fakturDto.tanggal_diterima)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = fakturDto.nama_pelanggan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = fakturDto.nomor_pelanggan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = fakturDto.nama_lembaga,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.CreditCard, contentDescription = null)
                Text(text = "Pembayaran ${fakturDto.sistem_pembayaran}", color = Color.Black)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Total Modal ", Modifier.weight(1f))
                Text(
                    text = fakturDto.total_modal.formatRupiah(),
                    fontSize = 17.sp,
                    color = Color.Black
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Total Faktur ", Modifier.weight(1f))
                Text(
                    text = fakturDto.total_faktur.formatRupiah(),
                    fontSize = 17.sp,
                    color = Color.Black
                )
            }

        }
    }
}

fun Long.formatRupiah(): String {
    val localeID = Locale("id", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    return numberFormat.format(this)
}