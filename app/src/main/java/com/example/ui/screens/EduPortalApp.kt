package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BottomNavBar
import com.example.ui.components.EduHeader
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SchoolViewModel

@Composable
fun EduPortalApp(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    // Collect Reactive StateFlow elements from SchoolViewModel
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    val students by viewModel.studentsList.collectAsStateWithLifecycle()
    val teachers by viewModel.teachersList.collectAsStateWithLifecycle()
    val classes by viewModel.classesList.collectAsStateWithLifecycle()
    val announcements by viewModel.announcementsList.collectAsStateWithLifecycle()
    val attendance by viewModel.attendanceList.collectAsStateWithLifecycle()
    val journals by viewModel.journalsList.collectAsStateWithLifecycle()
    val journalHistory by viewModel.journalHistoryList.collectAsStateWithLifecycle()
    val ebooks by viewModel.ebooksList.collectAsStateWithLifecycle()
    val exams by viewModel.examsList.collectAsStateWithLifecycle()
    val examResults by viewModel.examResultsList.collectAsStateWithLifecycle()
    val mediaList by viewModel.activityMediaList.collectAsStateWithLifecycle()
    val ahlakMonitors by viewModel.ahlakMonitorsList.collectAsStateWithLifecycle()
    val characterTraits by viewModel.characterTraitsList.collectAsStateWithLifecycle()
    val todos by viewModel.todosList.collectAsStateWithLifecycle()
    val notifications by viewModel.notificationsList.collectAsStateWithLifecycle()
    
    val activeChildId by viewModel.activeChildId.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()

    MyApplicationTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                EduHeader(
                    currentUser = currentUser,
                    syncState = syncStatus,
                    notifications = notifications,
                    onSyncTrigger = { viewModel.startDatabaseSync() },
                    onRoleSelected = { viewModel.switchRoleTo(it) },
                    onNavigateToProfile = { viewModel.navigateTo(SchoolViewModel.Screen.Profile) },
                    onNotificationClear = { viewModel.clearNotifications() }
                )
            },
            bottomBar = {
                currentUser?.let { user ->
                    BottomNavBar(
                        activeRole = user.role,
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Crossfade animation wrapper for smooth edge-to-edge screen transitions under 300ms
                Crossfade(
                    targetState = currentScreen,
                    label = "screen_nav_transition"
                ) { screen ->
                    when (screen) {
                        is SchoolViewModel.Screen.Dashboard -> {
                            currentUser?.let { user ->
                                MainDashboardScreen(
                                    viewModel = viewModel,
                                    currentUser = user,
                                    students = students,
                                    teachers = teachers,
                                    classes = classes,
                                    announcements = announcements,
                                    attendance = attendance,
                                    ebooks = ebooks,
                                    todos = todos,
                                    examResults = examResults,
                                    activeChildId = activeChildId,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                        is SchoolViewModel.Screen.DataSiswa -> {
                            DataSiswaScreen(
                                viewModel = viewModel,
                                students = students,
                                classes = classes
                            )
                        }
                        is SchoolViewModel.Screen.DataGuru -> {
                            DataGuruScreen(
                                viewModel = viewModel,
                                teachers = teachers
                            )
                        }
                        is SchoolViewModel.Screen.DataKelas -> {
                            DataKelasScreen(
                                viewModel = viewModel,
                                classes = classes,
                                teachers = teachers
                            )
                        }
                        is SchoolViewModel.Screen.Absensi -> {
                            AbsensiScreen(
                                viewModel = viewModel,
                                students = students,
                                classes = classes,
                                selectedClassId = selectedClassId
                            )
                        }
                        is SchoolViewModel.Screen.Jurnal -> {
                            JurnalScreen(
                                viewModel = viewModel,
                                journals = journals,
                                history = journalHistory,
                                classes = classes
                            )
                        }
                        is SchoolViewModel.Screen.Ebook -> {
                            currentUser?.let { user ->
                                EbookScreen(
                                    viewModel = viewModel,
                                    currentUser = user,
                                    ebooks = ebooks
                                )
                            }
                        }
                        is SchoolViewModel.Screen.Ujian -> {
                            currentUser?.let { user ->
                                UjianScreen(
                                    viewModel = viewModel,
                                    currentUser = user,
                                    classes = classes,
                                    students = students,
                                    exams = exams,
                                    results = examResults
                                )
                            }
                        }
                        is SchoolViewModel.Screen.Media -> {
                            currentUser?.let { user ->
                                MediaGalleryScreen(
                                    viewModel = viewModel,
                                    currentUser = user,
                                    classes = classes,
                                    students = students,
                                    mediaList = mediaList
                                )
                            }
                        }
                        is SchoolViewModel.Screen.Akhlak -> {
                            currentUser?.let { user ->
                                AkhlakScreen(
                                    viewModel = viewModel,
                                    currentUser = user,
                                    students = students,
                                    monitors = ahlakMonitors,
                                    activeChildId = activeChildId,
                                    traits = characterTraits
                                )
                            }
                        }
                        is SchoolViewModel.Screen.CalendarTodo -> {
                            CalendarTodoScreen(
                                viewModel = viewModel,
                                todos = todos
                            )
                        }
                        is SchoolViewModel.Screen.Profile -> {
                            currentUser?.let { user ->
                                UserProfileScreen(
                                    viewModel = viewModel,
                                    currentUser = user
                                )
                            }
                        }
                        is SchoolViewModel.Screen.Settings -> {
                            currentUser?.let { user ->
                                SettingsScreen(
                                    viewModel = viewModel,
                                    isDarkTheme = isDarkTheme,
                                    syncState = syncStatus,
                                    currentUser = user,
                                    onRoleSelected = { viewModel.switchRoleTo(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
