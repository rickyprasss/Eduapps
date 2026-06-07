package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.AcademicProgressLineChart
import com.example.ui.components.AttendanceCircleChart
import com.example.viewmodel.SchoolViewModel

@Composable
fun MainDashboardScreen(
    viewModel: SchoolViewModel,
    currentUser: UserEntity,
    students: List<StudentEntity>,
    teachers: List<TeacherEntity>,
    classes: List<ClassEntity>,
    announcements: List<AnnouncementEntity>,
    attendance: List<AttendanceEntity>,
    ebooks: List<EbookEntity>,
    todos: List<TodoEntity>,
    examResults: List<ExamResultEntity>,
    activeChildId: String,
    onNavigate: (SchoolViewModel.Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dashboard_root")
    ) {
        when (currentUser.role) {
            "ADMIN" -> AdminDashboard(viewModel, students, teachers, classes, announcements, todos, onNavigate)
            "GURU" -> TeacherDashboard(viewModel, students, classes, announcements, onNavigate)
            "ORANG_TUA" -> ParentDashboard(viewModel, currentUser, students, announcements, attendance, examResults, activeChildId)
        }
    }
}

// ================= ADMIN DASHBOARD =================
@Composable
fun AdminDashboard(
    viewModel: SchoolViewModel,
    students: List<StudentEntity>,
    teachers: List<TeacherEntity>,
    classes: List<ClassEntity>,
    announcements: List<AnnouncementEntity>,
    todos: List<TodoEntity>,
    onNavigate: (SchoolViewModel.Screen) -> Unit
) {
    var showPostDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Stats Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selamat Datang, Admin!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pusat Kendali Pengelolaan Data Sekolah, Jadwal Ujian, Jurnal Kelas, dan Pengumuman Digital.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Analytical Counter Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Siswa",
                    count = students.size.toString(),
                    icon = Icons.Default.Groups,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).clickable { onNavigate(SchoolViewModel.Screen.DataSiswa) }
                )
                StatCard(
                    title = "Guru",
                    count = teachers.size.toString(),
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f).clickable { onNavigate(SchoolViewModel.Screen.DataGuru) }
                )
                StatCard(
                    title = "Kelas",
                    count = classes.size.toString(),
                    icon = Icons.Default.Room,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f).clickable { onNavigate(SchoolViewModel.Screen.DataKelas) }
                )
            }
        }

        // Quick Post Digital Bulletin
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pengumuman Sekolah",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Button(
                    onClick = { showPostDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Announcement Items
        if (announcements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada pengumuman digital.", color = Color.Gray)
                }
            }
        } else {
            items(announcements.take(3)) { ann ->
                AnnouncementItem(ann)
            }
        }

        // Direct Calendar Reminders
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kontekstual Pengingat (Google Kalender)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                TextButton(onClick = { onNavigate(SchoolViewModel.Screen.CalendarTodo) }) {
                    Text("Kelola Kalender")
                }
            }
        }

        val incompleteTodos = todos.filter { !it.isCompleted }
        if (incompleteTodos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Celebration, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hebat! Semua tugas administratif hari ini selesai.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(incompleteTodos.take(2)) { todo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = todo.isCompleted,
                            onCheckedChange = { viewModel.toggletodoCompletion(todo) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = todo.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${todo.date} - ${todo.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // MAKE ANNOUNCEMENT DIALOG
    if (showPostDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var targetRole by remember { mutableStateOf("ALL") }

        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Posting Pengumuman") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Pengumuman") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Isi Pengumuman") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Text("Target Penerima:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = targetRole == "ALL",
                            onClick = { targetRole = "ALL" },
                            label = { Text("Semua") }
                        )
                        FilterChip(
                            selected = targetRole == "GURU",
                            onClick = { targetRole = "GURU" },
                            label = { Text("Guru") }
                        )
                        FilterChip(
                            selected = targetRole == "ORANG_TUA",
                            onClick = { targetRole = "ORANG_TUA" },
                            label = { Text("Wali") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            viewModel.postAnnouncement(title, content, targetRole)
                            showPostDialog = false
                        }
                    }
                ) {
                    Text("Kirim")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ================= GURU / TEACHER DASHBOARD =================
@Composable
fun TeacherDashboard(
    viewModel: SchoolViewModel,
    students: List<StudentEntity>,
    classes: List<ClassEntity>,
    announcements: List<AnnouncementEntity>,
    onNavigate: (SchoolViewModel.Screen) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher welcome headers
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Beranda Guru, Semangat Mengajar!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kelola absensi kehadiran siswa secara instan, tulis jurnal kelas, posting foto kegiatan siswa dan jadwalkan ujian kelas Anda.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Quick feature navigators grids
        item {
            Text(
                text = "Pintasan Menu Utama",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShortcutMenuCard(
                    title = "Isi Kehadiran",
                    description = "Ceklis absensi harian",
                    icon = Icons.Filled.HowToReg,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigate(SchoolViewModel.Screen.Absensi) },
                    modifier = Modifier.weight(1f)
                )
                ShortcutMenuCard(
                    title = "Jurnal Kelas",
                    description = "Catat topik & revisi",
                    icon = Icons.Filled.MenuBook,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { onNavigate(SchoolViewModel.Screen.Jurnal) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShortcutMenuCard(
                    title = "Dokumen Kegiatan",
                    description = "Unggah foto & video siswa",
                    icon = Icons.Filled.PhotoLibrary,
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = { onNavigate(SchoolViewModel.Screen.Media) },
                    modifier = Modifier.weight(1f)
                )
                ShortcutMenuCard(
                    title = "Jadwal & Hasil Ujian",
                    description = "Evaluasi modul akademik",
                    icon = Icons.Filled.Assignment,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { onNavigate(SchoolViewModel.Screen.Ujian) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Teacher Announcements Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bulletin Digital Terkini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        val teacherVisNotifs = announcements.filter { it.targetRole == "ALL" || it.targetRole == "GURU" }
        if (teacherVisNotifs.isEmpty()) {
            item {
                Text(text = "Belum ada pengumuman digital.", color = Color.Gray)
            }
        } else {
            items(teacherVisNotifs.take(2)) { ann ->
                AnnouncementItem(ann)
            }
        }
    }
}

// ================= ORANG TUA / PARENT DASHBOARD =================
@Composable
fun ParentDashboard(
    viewModel: SchoolViewModel,
    currentUser: UserEntity,
    students: List<StudentEntity>,
    announcements: List<AnnouncementEntity>,
    attendance: List<AttendanceEntity>,
    examResults: List<ExamResultEntity>,
    activeChildId: String
) {
    // Filter parent's kids
    val parentKidIds = currentUser.childrenIds.split(",").filter { it.isNotEmpty() }
    val myKids = students.filter { parentKidIds.contains(it.id) }

    // Selected child info
    val selectedChild = students.find { it.id == activeChildId } ?: myKids.firstOrNull()

    // Calculated stats
    val parentKidAttendance = selectedChild?.let { kid ->
        val logs = attendance.filter { it.studentId == kid.id }
        if (logs.isNotEmpty()) {
            val presenceCount = logs.count { it.status == "Hadir" }.toFloat()
            presenceCount / logs.size
        } else {
            0.98f // high optimistic default
        }
    } ?: 0.98f

    val parentKidGrades = selectedChild?.let { kid ->
        examResults.filter { it.studentId == kid.id }
    } ?: emptyList()

    val reportText by viewModel.lastGeneratedReport.collectAsState()
    val mediaList by viewModel.activityMediaList.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Child selector tabs for parent with multiple child accounts
        if (myKids.size > 1) {
            item {
                Column {
                    Text(
                        text = "PILIH ANAK ANDA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(myKids) { kid ->
                            val isSelected = kid.id == activeChildId
                            val shape = RoundedCornerShape(16.dp)
                            Box(
                                modifier = Modifier
                                    .clip(shape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .then(
                                        if (!isSelected) Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = shape
                                        ) else Modifier
                                    )
                                    .clickable { viewModel.setActiveChild(kid.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom square letter badge
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = kid.name.firstOrNull()?.toString()?.uppercase() ?: "S",
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${kid.name} (Kelas ${kid.classId})",
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected child bio and stats header
        selectedChild?.let { kid ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = kid.name.firstOrNull()?.toString()?.uppercase() ?: "S",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = kid.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "NISN: ${kid.nisn} | Kelas: ${kid.classId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            // Interactive Graphics / Charts Layer
            item {
                Text(
                    text = "Dashboard Analitik Akademik & Presensi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Donut Arc for Presence Ratio
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Rasio Kehadiran",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AttendanceCircleChart(
                                rate = parentKidAttendance,
                                modifier = Modifier
                                    .size(90.dp)
                                    .padding(4.dp)
                            )
                        }
                    }

                    // Progress Chart for Academic Grades
                    Card(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(180.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Progres Nilai Ujian",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (parentKidGrades.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Belum ada input nilai.", color = Color.Gray, fontSize = 11.sp)
                                }
                            } else {
                                val chartScores = parentKidGrades.map { it.score }
                                val chartLabels = parentKidGrades.map { it.subject.take(4) }
                                AcademicProgressLineChart(
                                    scores = chartScores,
                                    labels = chartLabels,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // High Fidelity Emerald & Amber Monitoring Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                    val emeraldBg = if (isDark) Color(0xFF062F24) else Color(0xFFECFDF5)
                    val emeraldBorder = if (isDark) Color(0xFF059669) else Color(0xFFD1FAE5)
                    val emeraldText = if (isDark) Color(0xFFA7F3D0) else Color(0xFF047857)
                    val emeraldDeepText = if (isDark) Color(0xFF34D399) else Color(0xFF064E3B)

                    val amberBg = if (isDark) Color(0xFF3B1E08) else Color(0xFFFEF3C7)
                    val amberBorder = if (isDark) Color(0xFFD97706) else Color(0xFFFDE68A)
                    val amberText = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                    val amberDeepText = if (isDark) Color(0xFFFBBF24) else Color(0xFF78350F)

                    // Attendance Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = emeraldBg),
                        border = BorderStroke(1.dp, emeraldBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "KEHADIRAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = emeraldText
                            )
                            Column {
                                Text(
                                    text = "${(parentKidAttendance * 100).toInt()}%",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = emeraldDeepText,
                                    lineHeight = 26.sp
                                )
                                Text(
                                    text = "Hadir Tepat Waktu",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldText,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    // Akhlak Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = amberBg),
                        border = BorderStroke(1.dp, amberBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "MONITORING AKHLAK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = amberText
                            )
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    repeat(5) { i ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (i < 4) amberDeepText else amberBorder,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Sangat Baik",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = amberDeepText,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- JURNAL DOKUMENTASI KEGIATAN SISWA ---
            val childMedia = mediaList.filter { it.studentId == kid.id || it.classId == kid.classId }
            if (childMedia.isNotEmpty()) {
                item {
                    Text(
                        text = "Dokumentasi Kegiatan Siswa Terbaru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(childMedia) { media ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { viewModel.navigateTo(SchoolViewModel.Screen.Media) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = media.mediaUri,
                                        contentDescription = media.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = media.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Oleh: ${media.uploadedBy}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "Kelas: ${media.className}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Realtime School Announcements
            item {
                Text(
                    text = "Pengumuman Sekolah Real-time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            val parentVisNotifs = announcements.filter { it.targetRole == "ALL" || it.targetRole == "ORANG_TUA" }
            if (parentVisNotifs.isEmpty()) {
                item {
                    Text(text = "Belum ada pengumuman digital.", color = Color.Gray)
                }
            } else {
                items(parentVisNotifs.take(1)) { ann ->
                    AnnouncementItem(ann)
                }
            }

            // AUTOMATED BULANAN REPORT EXPORTER (API INTEGRATOR MOCK)
            item {
                Text(
                    text = "Laporan Bulanan Elektronik (Integrasi API)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kirim Evaluasi Otomatis",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { viewModel.triggerMonthlyReportAPI(kid.id) },
                                modifier = Modifier.testTag("generate_report_button")
                            ) {
                                Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Report", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fitur ini mengintegrasikan server portal akademik ke REST API perangkat handphone wali murid agar secara realtime mendapatkan dokumen rekapitulasi data absensi, nilai ujian dan indeks perkembangan moral.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (reportText.isNotEmpty() && reportText.contains(kid.name)) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = reportText,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= SHARABLE COMPONENT WIDGETS =================
@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = color,
                    fontSize = 22.sp
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ShortcutMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun AnnouncementItem(ann: AnnouncementEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ann.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.widthIn(max = 200.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Untuk: ${if (ann.targetRole == "ALL") "Semua" else ann.targetRole}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ann.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Diterbitkan oleh: ${ann.authorName} (${ann.authorRole})",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ann.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
