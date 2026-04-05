package com.finbaby.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finbaby.app.ui.components.FinBabyBottomNav
import com.finbaby.app.ui.components.FinBabyTopBar
import com.finbaby.app.util.CurrencyFormatter

@Composable
fun SettingsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCategoryManagement: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.backupMessage, uiState.exportMessage) {
        val msg = uiState.backupMessage ?: uiState.exportMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Salary edit dialog
    if (uiState.isEditingSalary) {
        AlertDialog(
            onDismissRequest = { viewModel.setEditingSalary(false) },
            title = { Text("Edit Salary") },
            text = {
                OutlinedTextField(
                    value = uiState.salaryInput,
                    onValueChange = { viewModel.updateSalaryInput(it) },
                    label = { Text("Monthly Salary") },
                    prefix = { Text("\u20B9") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.saveSalary() }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEditingSalary(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            FinBabyTopBar(title = "Settings")
        },
        bottomBar = {
            FinBabyBottomNav(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile Section
            SectionHeader("Profile")
            SettingsItem(
                icon = Icons.Filled.CurrencyRupee,
                title = "Salary",
                subtitle = uiState.profile?.let { CurrencyFormatter.format(it.salary) } ?: "Not set",
                onClick = { viewModel.setEditingSalary(true) }
            )
            SettingsItem(
                icon = Icons.Filled.CalendarMonth,
                title = "Salary Date",
                subtitle = "Day ${uiState.profile?.salaryDate ?: 1} of each month"
            )
            SettingsItem(
                icon = Icons.Filled.Payments,
                title = "Currency",
                subtitle = uiState.profile?.currency ?: "\u20B9"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Budget Strategy Section
            SectionHeader("Budget Strategy")
            SettingsItem(
                icon = Icons.Filled.PieChart,
                title = "Budget Split",
                subtitle = uiState.profile?.let {
                    "${it.needsPercent}% Needs / ${it.wantsPercent}% Wants / ${it.savingsPercent}% Savings"
                } ?: "50/30/20"
            )
            SettingsItem(
                icon = Icons.Filled.Category,
                title = "Manage Categories",
                subtitle = "Add, edit, or disable categories",
                onClick = onCategoryManagement,
                showArrow = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Security Section
            SectionHeader("Security")
            SettingsToggle(
                icon = Icons.Filled.Lock,
                title = "App Lock",
                subtitle = "Require biometric/PIN to open",
                checked = uiState.isAppLockEnabled,
                onCheckedChange = { viewModel.toggleAppLock(it) }
            )
            SettingsToggle(
                icon = Icons.Filled.VisibilityOff,
                title = "Hide Amounts",
                subtitle = "Mask amounts on home screen",
                checked = uiState.isHideAmountsEnabled,
                onCheckedChange = { viewModel.toggleHideAmounts(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Data Management Section
            SectionHeader("Data Management")
            SettingsItem(
                icon = Icons.Filled.Backup,
                title = "Backup Data",
                subtitle = "Save data as JSON to Downloads",
                onClick = { viewModel.backup() }
            )
            SettingsItem(
                icon = Icons.Filled.Restore,
                title = "Restore Data",
                subtitle = "Restore from backup file",
                onClick = { viewModel.restore() }
            )
            SettingsItem(
                icon = Icons.Filled.TableChart,
                title = "Export CSV",
                subtitle = "Export transactions as CSV",
                onClick = { viewModel.exportCsv() }
            )
            SettingsItem(
                icon = Icons.Filled.PictureAsPdf,
                title = "Export PDF",
                subtitle = "Export monthly report as PDF"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preferences Section
            SectionHeader("Preferences")
            SettingsToggle(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = "Use dark theme",
                checked = uiState.isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
            SettingsItem(
                icon = Icons.Filled.Language,
                title = "Language",
                subtitle = uiState.language
            )
            SettingsToggle(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Daily reminders & budget alerts",
                checked = uiState.isNotificationsEnabled,
                onCheckedChange = { viewModel.toggleNotifications(it) }
            )
            SettingsItem(
                icon = Icons.Filled.Info,
                title = "About",
                subtitle = "FinBaby v1.0.0"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
