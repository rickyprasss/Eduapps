package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.NotificationEntity
import com.example.data.UserEntity
import com.example.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduHeader(
    currentUser: UserEntity?,
    syncState: com.example.data.AppRepository.SyncState,
    notifications: List<NotificationEntity>,
    onSyncTrigger: () -> Unit,
    onRoleSelected: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNotificationClear: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }

    val unreadNotifs = notifications.filter { !it.isRead }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Column(modifier = Modifier.padding(start = 4.dp)) {
                val roleLabel = when (currentUser?.role) {
                    "ADMIN" -> "Dashboard Admin"
                    "GURU" -> "Dashboard Pendidik"
                    "ORANG_TUA" -> "Dashboard Wali Murid"
                    else -> "EduPortal"
                }
                Text(
                    text = roleLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.6.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
                Text(
                    text = if (currentUser != null) "Halo, ${currentUser.name}" else "Selamat Datang",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            // SYNC BUTTON
            IconButton(
                onClick = onSyncTrigger,
                modifier = Modifier.testTag("sync_button")
            ) {
                if (syncState == com.example.data.AppRepository.SyncState.Synchronizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = "Sync",
                        tint = if (syncState == com.example.data.AppRepository.SyncState.Offline) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // NOTIFICATION BUTTON
            Box {
                IconButton(
                    onClick = { showNotifDialog = true },
                    modifier = Modifier.testTag("notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (unreadNotifs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .background(MaterialTheme.colorScheme.error, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadNotifs.size.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // PLAYGROUND ROLE SWITCHER BUTTON
            Button(
                onClick = { showRoleMenu = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .testTag("role_switcher_pill")
            ) {
                Text(
                    text = currentUser?.role ?: "Switch",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = showRoleMenu,
                onDismissRequest = { showRoleMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("ADMIN (Sudarsono)") },
                    onClick = {
                        onRoleSelected("ADMIN")
                        showRoleMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) }
                )
                DropdownMenuItem(
                    text = { Text("GURU (Budi Santoso)") },
                    onClick = {
                        onRoleSelected("GURU")
                        showRoleMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.School, null) }
                )
                DropdownMenuItem(
                    text = { Text("ORANG TUA (Siti Rahma)") },
                    onClick = {
                        onRoleSelected("ORANG_TUA")
                        showRoleMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.People, null) }
                )
            }

            // USER AVATAR TO EDIT PROFILE
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onNavigateToProfile() }
                    .testTag("profile_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser?.profilePic?.isNotEmpty() == true) {
                    Text(
                        text = currentUser.profilePic, // emoji avatar picker support
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        text = (currentUser?.name?.firstOrNull() ?: 'U').toString().uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    )

    // NOTIFICATIONS DIALOG LIST (SIMULATED REAL-TIME PUSH ALERTS)
    if (showNotifDialog) {
        Dialog(onDismissRequest = { showNotifDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifikasi Realtime",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showNotifDialog = false }) {
                            Icon(Icons.Default.Close, "Tutup")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada notifikasi push masuk",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(notifications.size) { index ->
                                val notif = notifications[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(
                                            color = if (notif.isRead) Color.Transparent else MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.25f
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    val icon = when (notif.category) {
                                        "ATTENDANCE" -> Icons.Default.FactCheck
                                        "ANNOUNCEMENT" -> Icons.Default.Campaign
                                        "EXAM" -> Icons.Default.AssignmentLate
                                        else -> Icons.Default.Notifications
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = notif.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                onNotificationClear()
                                showNotifDialog = false
                            },
                            enabled = notifications.isNotEmpty()
                        ) {
                            Text("Tandai Semua Terbaca")
                        }
                        Button(onClick = { showNotifDialog = false }) {
                            Text("Selesai")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    activeRole: String,
    currentScreen: SchoolViewModel.Screen,
    onNavigate: (SchoolViewModel.Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        tonalElevation = 8.dp
    ) {
        // Universal Dashboard
        NavigationBarItem(
            selected = currentScreen == SchoolViewModel.Screen.Dashboard,
            onClick = { onNavigate(SchoolViewModel.Screen.Dashboard) },
            label = { Text("Dashboard") },
            icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
            modifier = Modifier.testTag("nav_dashboard")
        )

        // Submenus dependant on active role permissions
        when (activeRole) {
            "ADMIN" -> {
                NavigationBarItem(
                    selected = currentScreen == SchoolViewModel.Screen.DataSiswa,
                    onClick = { onNavigate(SchoolViewModel.Screen.DataSiswa) },
                    label = { Text("Siswa") },
                    icon = { Icon(Icons.Default.People, "Siswa") },
                    modifier = Modifier.testTag("nav_siswa")
                )
                NavigationBarItem(
                    selected = currentScreen == SchoolViewModel.Screen.CalendarTodo,
                    onClick = { onNavigate(SchoolViewModel.Screen.CalendarTodo) },
                    label = { Text("Kalender") },
                    icon = { Icon(Icons.Default.CalendarMonth, "Todo") },
                    modifier = Modifier.testTag("nav_todo")
                )
            }
            "GURU" -> {
                NavigationBarItem(
                    selected = currentScreen == SchoolViewModel.Screen.Absensi,
                    onClick = { onNavigate(SchoolViewModel.Screen.Absensi) },
                    label = { Text("Absensi") },
                    icon = { Icon(Icons.Default.HowToReg, "Absensi") },
                    modifier = Modifier.testTag("nav_absensi")
                )
                NavigationBarItem(
                    selected = currentScreen == SchoolViewModel.Screen.Jurnal,
                    onClick = { onNavigate(SchoolViewModel.Screen.Jurnal) },
                    label = { Text("Jurnal") },
                    icon = { Icon(Icons.Default.MenuBook, "Jurnal") },
                    modifier = Modifier.testTag("nav_jurnal")
                )
            }
            "ORANG_TUA" -> {
                NavigationBarItem(
                    selected = currentScreen == SchoolViewModel.Screen.Akhlak,
                    onClick = { onNavigate(SchoolViewModel.Screen.Akhlak) },
                    label = { Text("Ahlaq") },
                    icon = { Icon(Icons.Default.Psychology, "Monitor") },
                    modifier = Modifier.testTag("nav_akhlak")
                )
            }
        }

        // Shared features: Ebooks & Settings/Media
        NavigationBarItem(
            selected = currentScreen == SchoolViewModel.Screen.Ebook,
            onClick = { onNavigate(SchoolViewModel.Screen.Ebook) },
            label = { Text("Perpus") },
            icon = { Icon(Icons.Default.LibraryBooks, "Ebook") },
            modifier = Modifier.testTag("nav_ebook")
        )
        
        NavigationBarItem(
            selected = currentScreen == SchoolViewModel.Screen.Settings,
            onClick = { onNavigate(SchoolViewModel.Screen.Settings) },
            label = { Text("Settings") },
            icon = { Icon(Icons.Default.Settings, "Settings") },
            modifier = Modifier.testTag("nav_settings")
        )
    }
}
