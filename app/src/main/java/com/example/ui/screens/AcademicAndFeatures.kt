package com.example.ui.screens

import androidx.compose.animation.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.viewmodel.SchoolViewModel

// ================= TEACHER: ATTENDANCE CHECKLIST =================
@Composable
fun AbsensiScreen(
    viewModel: SchoolViewModel,
    students: List<StudentEntity>,
    classes: List<ClassEntity>,
    selectedClassId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val filteredStudents = students.filter { it.classId == selectedClassId }
    val statuses = remember { mutableStateMapOf<String, String>() }
    val infoNotes = remember { mutableStateMapOf<String, String>() }

    // Initialize statuses if not preset
    LaunchedEffect(filteredStudents) {
        filteredStudents.forEach { student ->
            if (!statuses.containsKey(student.id)) {
                statuses[student.id] = "Hadir"
            }
        }
    }

    var showSuccessBanner by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("absensi_root")) {
        Text("Absensi Kelas Harian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Pilih Rombongan Belajar Kelas untuk Menginput Presensi Siswa", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        // Class Filters Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(classes) { cls ->
                val isSelected = cls.id == selectedClassId
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectClassId(cls.id) },
                    label = { Text(cls.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showSuccessBanner) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, "Sukses", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Absensi tersimpan! Notifikasi push telah dikirim instan ke gawai wali murid.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (filteredStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Tidak ada siswa terdaftar di kelas ini.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredStudents) { student ->
                    val curStatus = statuses[student.id] ?: "Hadir"
                    val curNote = infoNotes[student.id] ?: ""

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(student.name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(student.name, fontWeight = FontWeight.Bold)
                                    Text("NISN: ${student.nisn}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Status Checklist Selector Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Hadir", "Sakit", "Izin", "Alpa").forEach { opt ->
                                    val isPicked = curStatus == opt
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isPicked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { statuses[student.id] = opt }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = opt,
                                            color = if (isPicked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Expandable Custom Notes (e.g. "Suhu 38C")
                            OutlinedTextField(
                                value = curNote,
                                onValueChange = { infoNotes[student.id] = it },
                                placeholder = { Text("Keterangan opsional (e.g. sakit flu)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val records = filteredStudents.map { it to (statuses[it.id] ?: "Hadir") }
                viewModel.submitAttendance(records, infoNotes)
                showSuccessBanner = true
                // Auto hide banner
                coroutineScope.launch {
                    kotlinx.coroutines.delay(3500)
                    showSuccessBanner = false
                }
            },
            enabled = filteredStudents.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_attendance_btn")
        ) {
            Icon(Icons.Default.Save, null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kirim Presensi Sekarang", fontWeight = FontWeight.Bold)
        }
    }
}

// ================= CLASS JOURNAL WITH EDIT REVISION ARCHIVES =================
@Composable
fun JurnalScreen(
    viewModel: SchoolViewModel,
    journals: List<ClassJournalEntity>,
    history: List<JournalHistoryEntity>,
    classes: List<ClassEntity>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingHistoryJournalId by remember { mutableStateOf<Int?>(null) }
    var editingJournal by remember { mutableStateOf<ClassJournalEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("jurnal_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Jurnal Mengajar Kelas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Pencatatan materi & histori revisi transparan", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Catat Jurnal")
            }
        }

        if (journals.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Belum ada catatan jurnal kelas disusun.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(journals) { jrn ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(jrn.subject, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Text("${jrn.className} | Oleh: ${jrn.teacherName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(jrn.date, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Topik Bahasan:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(jrn.topic, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Catatan Pembelajaran:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(jrn.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = { viewingHistoryJournalId = jrn.id },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Histori Perubahan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { editingJournal = jrn },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revisi Jurnal", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // JOURNAL ADD DIALOG
    if (showAddDialog) {
        var clsId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "10-A") }
        var clsName by remember { mutableStateOf(classes.firstOrNull()?.name ?: "Kelas X-A") }
        var subject by remember { mutableStateOf("") }
        var topic by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Buat Catatan Jurnal Mengajar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pilih Kelas:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        classes.forEach { cls ->
                            FilterChip(
                                selected = clsId == cls.id,
                                onClick = {
                                    clsId = cls.id
                                    clsName = cls.name
                                },
                                label = { Text(cls.name) }
                            )
                        }
                    }
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran (e.g. Fisika)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topik Pokok Pembahasan") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Detail Catatan Kegiatan") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotEmpty() && topic.isNotEmpty()) {
                            viewModel.submitClassJournal(clsId, clsName, subject, topic, notes)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Kirim Jurnal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }

    // JOURNAL EDIT REVISION DIALOG
    if (editingJournal != null) {
        val jrn = editingJournal!!
        var topic by remember { mutableStateOf(jrn.topic) }
        var notes by remember { mutableStateOf(jrn.notes) }

        AlertDialog(
            onDismissRequest = { editingJournal = null },
            title = { Text("Revisi Catatan Jurnal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Kelas: ${jrn.className} • Mapel: ${jrn.subject}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topik Baru") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Detail Catatan Baru") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateClassJournal(jrn.copy(topic = topic, notes = notes))
                        editingJournal = null
                    }
                ) {
                    Text("Simpan & Arsipkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingJournal = null }) { Text("Batal") }
            }
        )
    }

    // JOURNAL HISTORY TIMELINE VIEW
    if (viewingHistoryJournalId != null) {
        val filteredHistory = history.filter { it.journalId == viewingHistoryJournalId }
        val parentJournal = journals.find { it.id == viewingHistoryJournalId }

        AlertDialog(
            onDismissRequest = { viewingHistoryJournalId = null },
            title = { Text("Histori Perubahan Transparan", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    Text("Mapel: ${parentJournal?.subject ?: ""} (${parentJournal?.className ?: ""})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredHistory.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat revisi pada jurnal ini.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredHistory) { hst ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${hst.actionType}: Oleh ${hst.editedBy}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(hst.editTimestamp)),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        if (hst.actionType == "EDIT") {
                                            Text("Topik Lama:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(hst.previousTopic, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                            Text("Topik Baru:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(hst.currentTopic, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        } else {
                                            Text("Topik Awal: ${hst.currentTopic}", fontSize = 11.sp)
                                            Text("Detail Awal: ${hst.currentNotes}", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewingHistoryJournalId = null }) {
                    Text("Selesai")
                }
            }
        )
    }
}

// ================= ADMIN & USER: EBOOKS LIBRARY =================
@Composable
fun EbookScreen(
    viewModel: SchoolViewModel,
    currentUser: UserEntity,
    ebooks: List<EbookEntity>
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    var searchKeyword by remember { mutableStateOf("") }

    val filteredEbooks = ebooks.filter {
        it.title.contains(searchKeyword, ignoreCase = true) || it.author.contains(searchKeyword, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("ebook_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Perpustakaan Akademik", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Unduh & baca modul ajar gratis offline", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (currentUser.role == "ADMIN") {
                Button(onClick = { showUploadDialog = true }) {
                    Icon(Icons.Default.CloudUpload, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unggah")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            placeholder = { Text("Cari judul buku atau penulis gratis...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredEbooks.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Buku tidak ditemukan.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredEbooks) { book ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Book, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Penulis: ${book.author} | Kategori: ${book.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("Ukuran File: ${book.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = {
                                // Simulate mock file download with simple system notification
                                viewModel.postAnnouncement("Pengunduhan Selesai", "Buku '${book.title}' telah diunduh offline dan siap dibuka.", "ALL")
                            }) {
                                Icon(Icons.Default.Download, "Unduh", tint = MaterialTheme.colorScheme.primary)
                            }

                            if (currentUser.role == "ADMIN") {
                                IconButton(onClick = { viewModel.deleteEbook(book.id) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        var title by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Buku Paket") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Unggah Modul E-Book Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Buku") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Nama Penulis/Penerbit") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori (e.g. Matematika)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Singkat Buku") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.uploadEbook(title, author, category, desc)
                            showUploadDialog = false
                        }
                    }
                ) {
                    Text("Unggah PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text("Batal") }
            }
        )
    }
}

// ================= SCHEDULING EXAMS AND SCORING GRADES =================
@Composable
fun UjianScreen(
    viewModel: SchoolViewModel,
    currentUser: UserEntity,
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    exams: List<ExamEntity>,
    results: List<ExamResultEntity>
) {
    var showExamDialog by remember { mutableStateOf(false) }
    var gradingExam by remember { mutableStateOf<ExamEntity?>(null) }
    var viewingResultsByExamId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("ujian_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Portal Evaluasi & Ujian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Penyusunan jadwal UTS/UAS dan penginputan nilai", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (currentUser.role == "GURU" || currentUser.role == "ADMIN") {
                Button(onClick = { showExamDialog = true }) {
                    Icon(Icons.Default.AddBox, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Jadwalkan")
                }
            }
        }

        if (exams.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Tidak ada jadwal ujian berlangsung saat ini.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(exams) { exm ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(exm.examName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                                    Text("Mapel: ${exm.subject} | Kelas: ${exm.className}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(exm.date, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Waktu: ${exm.time}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = { viewingResultsByExamId = exm.id },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.BarChart, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lihat Nilai Kelas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (currentUser.role == "GURU" || currentUser.role == "ADMIN") {
                                    Button(
                                        onClick = { gradingExam = exm },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Grade, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kasih Nilai", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // SCHEDULE EXAM
    if (showExamDialog) {
        var clsId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "10-A") }
        var clsName by remember { mutableStateOf(classes.firstOrNull()?.name ?: "Kelas X-A") }
        var subject by remember { mutableStateOf("") }
        var examName by remember { mutableStateOf("") }
        var dateVal by remember { mutableStateOf("2026-06-15") }
        var timeVal by remember { mutableStateOf("08:00 - 09:30") }

        AlertDialog(
            onDismissRequest = { showExamDialog = false },
            title = { Text("Jadwalkan Agenda Evaluasi Ujian") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih Kelas Mandatori:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        classes.forEach { cls ->
                            FilterChip(
                                selected = clsId == cls.id,
                                onClick = {
                                    clsId = cls.id
                                    clsName = cls.name
                                },
                                label = { Text(cls.name) }
                            )
                        }
                    }
                    OutlinedTextField(value = examName, onValueChange = { examName = it }, label = { Text("Nama Evaluasi (e.g. UTS Ganjil)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dateVal, onValueChange = { dateVal = it }, label = { Text("Tanggal Pelaksanaan") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = timeVal, onValueChange = { timeVal = it }, label = { Text("Jam Pelaksanaan") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (examName.isNotEmpty() && subject.isNotEmpty()) {
                            viewModel.scheduleExam(clsId, clsName, subject, examName, dateVal, timeVal)
                            showExamDialog = false
                        }
                    }
                ) {
                    Text("Publikasikan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExamDialog = false }) { Text("Batal") }
            }
        )
    }

    // INPUT STUDENTS SCORES FOR ACTIVE EXAMS
    if (gradingExam != null) {
        val exm = gradingExam!!
        val classmates = students.filter { it.classId == exm.classId }
        
        var selectedStudent by remember { mutableStateOf(classmates.firstOrNull()) }
        var scoreStr by remember { mutableStateOf("") }
        var notesVal by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { gradingExam = null },
            title = { Text("Input Skor Akademik Siswa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Agenda: ${exm.examName} • ${exm.subject}", fontWeight = FontWeight.Bold)
                    
                    if (classmates.isEmpty()) {
                        Text("Tidak ada siswa terdaftar di rombel kelas ini.", color = Color.Red)
                    } else {
                        Text("Pilih Siswa:", style = MaterialTheme.typography.labelSmall)
                        var expandedStudentMenu by remember { mutableStateOf(false) }

                        Box {
                            OutlinedButton(onClick = { expandedStudentMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedStudent?.name ?: "Pilih Nama Siswa")
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = expandedStudentMenu, onDismissRequest = { expandedStudentMenu = false }) {
                                classmates.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.name) },
                                        onClick = {
                                            selectedStudent = st
                                            expandedStudentMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(value = scoreStr, onValueChange = { scoreStr = it }, label = { Text("Nilai Siswa (Skala 100)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = notesVal, onValueChange = { notesVal = it }, label = { Text("Ulasan Khusus Kriteria") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val score = scoreStr.toDoubleOrNull()
                        val student = selectedStudent
                        if (score != null && student != null) {
                            viewModel.gradeStudentExam(exm.id, exm.examName, exm.subject, student.id, student.name, score, notesVal)
                            gradingExam = null
                        }
                    },
                    enabled = classmates.isNotEmpty()
                ) {
                    Text("Kirim Skor")
                }
            },
            dismissButton = {
                TextButton(onClick = { gradingExam = null }) { Text("Batal") }
            }
        )
    }

    // VIEW SUMMARY LIST OF SCORES
    if (viewingResultsByExamId != null) {
        val filteredResults = results.filter { it.examId == viewingResultsByExamId }
        val parentExam = exams.find { it.id == viewingResultsByExamId }

        AlertDialog(
            onDismissRequest = { viewingResultsByExamId = null },
            title = { Text("Hasil Penilaian Kelas") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                    Text("${parentExam?.examName} - ${parentExam?.subject}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredResults.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Belum ada skor siswa dimasukkan.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredResults) { res ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(res.studentName, fontWeight = FontWeight.Bold)
                                            if (res.notes.isNotEmpty()) {
                                                Text("Catatan: ${res.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                        Box(
                                            modifier = Modifier.size(44.dp).clip(CircleShape).background(if (res.score >= 75) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = res.score.toInt().toString(),
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (res.score >= 75) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewingResultsByExamId = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

// ================= PHOTOS & VIDEOS OF STUDENT ACTIVITIES GALLERY =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGalleryScreen(
    viewModel: SchoolViewModel,
    currentUser: UserEntity,
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    mediaList: List<ActivityMediaEntity>
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    var filterClassId by remember { mutableStateOf("") }
    var filterStudentId by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("media_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Dokumentasi Kegiatan Siswa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Galeri foto/video pembelajaran langsung", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (currentUser.role == "GURU" || currentUser.role == "ADMIN") {
                Button(
                    onClick = { showUploadDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kamera")
                }
            }
        }

        // Filter Controls Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kelas:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = filterClassId == "",
                        onClick = { 
                            filterClassId = ""
                            filterStudentId = ""
                        },
                        label = { Text("Semua Kelas", fontSize = 11.sp) }
                    )
                }
                items(classes) { cls ->
                    FilterChip(
                        selected = filterClassId == cls.id,
                        onClick = { 
                            filterClassId = cls.id
                            filterStudentId = "" // reset student filter
                        },
                        label = { Text(cls.name, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Student Filter (Only show if class is selected)
        if (filterClassId.isNotEmpty()) {
            val classStudents = students.filter { it.classId == filterClassId }
            if (classStudents.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Siswa:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = filterStudentId == "",
                                onClick = { filterStudentId = "" },
                                label = { Text("Semua Siswa", fontSize = 11.sp) }
                            )
                        }
                        items(classStudents) { st ->
                            FilterChip(
                                selected = filterStudentId == st.id,
                                onClick = { filterStudentId = st.id },
                                label = { Text(st.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val displayedMedia = mediaList.filter { item ->
            (filterClassId.isEmpty() || item.classId == filterClassId) &&
            (filterStudentId.isEmpty() || item.studentId == filterStudentId)
        }

        if (displayedMedia.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Tidak ada hasil dokumentasi.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedMedia) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            // Using standard AsyncImage from coil with beautiful random illustration fallbacks
                            AsyncImage(
                                model = item.mediaUri,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                contentScale = ContentScale.Crop
                            )

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Kelas: ${item.className}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                if (item.studentName.isNotEmpty()) {
                                    Text(
                                        text = "Siswa: ${item.studentName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Oleh ${item.uploadedBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 9.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        var title by remember { mutableStateOf("") }
        var clsId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "10-A") }
        var clsName by remember { mutableStateOf(classes.firstOrNull()?.name ?: "Kelas X-A") }
        var studentId by remember { mutableStateOf("") }
        var studentName by remember { mutableStateOf("") }

        val presetImages = listOf(
            "https://picsum.photos/id/201/600/400",
            "https://picsum.photos/id/202/600/400",
            "https://picsum.photos/id/203/600/400"
        )
        val selectedPreset = presetImages.random()

        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Unggah Foto Kegiatan Siswa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Deskripsi Singkat / Judul Kegiatan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pilih Kelas Terkait:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        classes.forEach { cls ->
                            FilterChip(
                                selected = clsId == cls.id,
                                onClick = {
                                    clsId = cls.id
                                    clsName = cls.name
                                    studentId = ""
                                    studentName = ""
                                },
                                label = { Text(cls.name) }
                            )
                        }
                    }

                    val classSiswa = students.filter { it.classId == clsId }
                    if (classSiswa.isNotEmpty()) {
                        Text("Hubungkan ke Siswa Khusus (Opsional):", style = MaterialTheme.typography.labelSmall)
                        var expandSiswa by remember { mutableStateOf(false) }
                        
                        Box {
                            OutlinedButton(onClick = { expandSiswa = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (studentName.isEmpty()) "Semua Siswa Terkait" else studentName)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = expandSiswa, onDismissRequest = { expandSiswa = false }) {
                                DropdownMenuItem(
                                    text = { Text("Semua Siswa Terkait") },
                                    onClick = {
                                        studentId = ""
                                        studentName = ""
                                        expandSiswa = false
                                    }
                                )
                                classSiswa.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.name) },
                                        onClick = {
                                            studentId = st.id
                                            studentName = st.name
                                            expandSiswa = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lampiran Sumber Gambar: Preset Simulasi Lensa Digital Cerdas Cermat",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.uploadMedia(
                                title = title,
                                classId = clsId,
                                className = clsName,
                                studentId = studentId,
                                studentName = studentName,
                                mediaUri = selectedPreset,
                                isVideo = false
                            )
                            showUploadDialog = false
                        }
                    }
                ) {
                    Text("Unggah Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text("Batal") }
            }
        )
    }
}
