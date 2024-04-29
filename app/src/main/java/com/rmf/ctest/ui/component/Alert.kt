package com.rmf.ctest.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(shape: Shape = RoundedCornerShape(20.dp)) {
    AlertDialog(
        onDismissRequest = { },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background, shape = shape)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical =16.dp, horizontal = 24.dp)
                .defaultMinSize(minHeight = 64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Harap tunggu...")
        }
    }
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Oke")
            }
        },
        text = {
            Text(text = message)
        },
        title = {
            Text(text = "Upss!")
        },
        icon = {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null)
        }
    )
}

@Composable
fun SuccessDialog(message: String, onDismiss: () -> Unit, onClickConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onClickConfirm) {
                Text(text = "Oke")
            }
        },
        text = {
            Text(text = message)
        },
        icon = {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
        }
    )
}
