package com.tisosoft.minimalpersonalos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.data.Contact
import com.tisosoft.minimalpersonalos.model.VisualProfile

@Composable
fun ContactActionDialog(
    contact: Contact,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    primaryColor: Color,
    visualProfile: VisualProfile = VisualProfile.SIMPLE
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                AvatarCircle(
                    displayName = contact.displayName,
                    photoUri = contact.photoUri,
                    primaryColor = primaryColor,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = contact.displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Phone Number
                Text(
                    text = contact.phoneNumber,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Actions
                if (visualProfile == VisualProfile.MINIMALISTIC) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionIconButton(
                            icon = Icons.Default.Call,
                            contentDescription = stringResource(R.string.contact_action_call),
                            onClick = onCall,
                            primaryColor = primaryColor
                        )
                        ActionIconButton(
                            icon = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.contact_action_edit),
                            onClick = onEdit,
                            primaryColor = primaryColor
                        )
                        ActionIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.contact_action_delete),
                            onClick = onDelete,
                            primaryColor = Color.Red
                        )
                    }
                } else {
                    Button(
                        onClick = onCall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text(
                            stringResource(R.string.contact_action_call).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 24.dp),
                        thickness = DividerDefaults.Thickness,
                        color = Color.LightGray
                    )

                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                            .copy(brush = androidx.compose.ui.graphics.SolidColor(primaryColor))
                    ) {
                        Text(stringResource(R.string.contact_action_edit))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            stringResource(R.string.contact_action_delete),
                            color = Color.Red
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text(stringResource(R.string.cancel), color = primaryColor)
            }
        }
    )
}

@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.CircleShape,
        color = primaryColor.copy(alpha = 0.1f),
        modifier = Modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = primaryColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

