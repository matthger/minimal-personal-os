package com.tisosoft.minimalpersonalos.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tisosoft.minimalpersonalos.R

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    primaryColor: Color,
    onButtonClick: (() -> Unit)? = null,
    title: String = stringResource(R.string.contact_action_delete),
    text: String = stringResource(R.string.contact_delete_confirm)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = {
                    onButtonClick?.invoke()
                    onConfirm()
                },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.yes), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onButtonClick?.invoke()
                    onDismiss()
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text(stringResource(R.string.cancel), color = primaryColor)
            }
        }
    )
}
