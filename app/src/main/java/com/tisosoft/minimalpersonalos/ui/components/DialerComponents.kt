package com.tisosoft.minimalpersonalos.ui.components

import android.telephony.PhoneNumberUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tisosoft.minimalpersonalos.R
import java.util.Locale

@Composable
fun DialerDisplay(
    number: String,
    modifier: Modifier = Modifier
) {
    val formattedNumber = if (number.isEmpty()) "" else {
        PhoneNumberUtils.formatNumber(number, Locale.getDefault().country) ?: number
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = formattedNumber,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun DialerKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onCallClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val buttonSize = (maxWidth / 4.2f).coerceIn(60.dp, 92.dp)
        val spacing = (maxHeight / 50f).coerceIn(4.dp, 16.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("*", "0", "#")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { digit ->
                        DialerButton(
                            text = digit,
                            onClick = { onDigitClick(digit) },
                            size = buttonSize
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder to keep alignment
                Box(modifier = Modifier.size(buttonSize))

                // Call Button
                DialerIconButton(
                    icon = Icons.Default.Call,
                    contentDescription = stringResource(R.string.contact_action_call),
                    onClick = onCallClick,
                    containerColor = primaryColor,
                    iconTint = Color.White,
                    size = buttonSize * 1.1f // Slightly larger call button
                )

                // Delete Button
                DialerIconButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.dialer_delete_digit),
                    onClick = onDeleteClick,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DialerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 92.dp
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = (size.value * 0.35f).sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DialerIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color,
    iconTint: Color = Color.Black,
    size: androidx.compose.ui.unit.Dp = 96.dp
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.4f),
                tint = iconTint
            )
        }
    }
}
