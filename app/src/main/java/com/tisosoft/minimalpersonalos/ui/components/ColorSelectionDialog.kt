package com.tisosoft.minimalpersonalos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.ui.theme.*

data class ColorOption(val nameResId: Int, val color: Color)

val availableColors = listOf(
    ColorOption(R.string.color_purple, PrimaryPurple),
    ColorOption(R.string.color_pink, PrimaryPink),
    ColorOption(R.string.color_ochre, PrimaryOchre),
    ColorOption(R.string.color_mint, PrimaryMint)
)

@Composable
fun ColorSelectionDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
    onPreview: (Color) -> Unit,
    onItemClick: () -> Unit,
    primaryColor: Color
) {
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(stringResource(R.string.select_primary_color)) },
        text = {
            Column {
                availableColors.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onItemClick()
                                selectedColor = option.color
                                onPreview(option.color)
                            }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = (selectedColor.toArgb() == option.color.toArgb()),
                            onClick = {
                                onItemClick()
                                selectedColor = option.color
                                onPreview(option.color)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(option.color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(option.nameResId),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedColor) },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
