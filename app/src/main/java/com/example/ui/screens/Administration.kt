package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.SchoolViewModel

// ================= DATA SISWA (ADMIN) =================
@Composable
fun DataSiswaScreen(viewModel: SchoolViewModel, students: List<StudentEntity>, classes: List<ClassEntity>) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("data_siswa_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Kelola Data Siswa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Total terdaftar: ${students.size} siswa", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah")
            }
        }

        if (students.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Belum ada data siswa. Silahkan tambahkan.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(students) { student ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(student.name.first().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("NISN: ${student.nisn} | Kelas: ${student.classId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("Wali: ${student.parentName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { editingStudent = student }) {
                                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteStudent(student.id) }) {
                                Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingStudent != null) {
        val student = editingStudent
        var id by remember { mutableStateOf(student?.id ?: "") }
        var name by remember { mutableStateOf(student?.name ?: "") }
        var nisn by remember { mutableStateOf(student?.nisn ?: "") }
        var classId by remember { mutableStateOf(student?.classId ?: classes.firstOrNull()?.id ?: "10-A") }
        var parentId by remember { mutableStateOf(student?.parentId ?: "ortu1") }
        var parentName by remember { mutableStateOf(student?.parentName ?: "Siti Rahma") }
        var birthDate by remember { mutableStateOf(student?.birthDate ?: "2010-01-01") }
        var address by remember { mutableStateOf(student?.address ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingStudent = null
            },
            title = { Text(if (student != null) "Edit Siswa" else "Tambah Siswa") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = id,
                            onValueChange = { id = it },
                            label = { Text("ID Siswa (Unik)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = student == null
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Siswa") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = nisn,
                            onValueChange = { nisn = it },
                            label = { Text("NISN") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Pilih Kelas:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            classes.forEach { cls ->
                                FilterChip(
                                    selected = classId == cls.id,
                                    onClick = { classId = cls.id },
                                    label = { Text(cls.name) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it },
                            label = { Text("Nama Orang Tua") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Alamat Rumah") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (id.isNotEmpty() && name.isNotEmpty()) {
                            val newStudent = StudentEntity(
                                id = id,
                                name = name,
                                nisn = nisn,
                                classId = classId,
                                parentId = parentId,
                                parentName = parentName,
                                birthDate = birthDate,
                                address = address
                            )
                            viewModel.addOrUpdateStudent(newStudent)
                            showAddDialog = false
                            editingStudent = null
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingStudent = null
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ================= DATA GURU (ADMIN) =================
@Composable
fun DataGuruScreen(viewModel: SchoolViewModel, teachers: List<TeacherEntity>) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<TeacherEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("data_guru_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Kelola Data Guru", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Total pengajar: ${teachers.size} guru", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah")
            }
        }

        if (teachers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Belum ada data guru.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(teachers) { teacher ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(teacher.name.first().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(teacher.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("NIP: ${teacher.nip} | Mapel: ${teacher.subject}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { editingTeacher = teacher }) {
                                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteTeacher(teacher.id) }) {
                                Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingTeacher != null) {
        val teacher = editingTeacher
        var id by remember { mutableStateOf(teacher?.id ?: "") }
        var name by remember { mutableStateOf(teacher?.name ?: "") }
        var nip by remember { mutableStateOf(teacher?.nip ?: "") }
        var subject by remember { mutableStateOf(teacher?.subject ?: "") }
        var phone by remember { mutableStateOf(teacher?.phone ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingTeacher = null
            },
            title = { Text(if (teacher != null) "Edit Pengajar" else "Tambah Pengajar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("ID Guru (Unik)") }, modifier = Modifier.fillMaxWidth(), enabled = teacher == null)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nip, onValueChange = { nip = it }, label = { Text("NIP") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran Utama") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (id.isNotEmpty() && name.isNotEmpty()) {
                            val newTeacher = TeacherEntity(id, name, nip, subject, phone)
                            viewModel.addOrUpdateTeacher(newTeacher)
                            showAddDialog = false
                            editingTeacher = null
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; editingTeacher = null }) { Text("Batal") }
            }
        )
    }
}

// ================= DATA KELAS (ADMIN) =================
@Composable
fun DataKelasScreen(viewModel: SchoolViewModel, classes: List<ClassEntity>, teachers: List<TeacherEntity>) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("data_kelas_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Kelola Data Kelas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Total kelas aktif: ${classes.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah")
            }
        }

        if (classes.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Belum ada data rombongan kelas.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(classes) { cls ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Class, null, tint = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cls.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Wali Kelas: ${cls.teacherName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.deleteClass(cls.id) }) {
                                Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var id by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var teacherId by remember { mutableStateOf(teachers.firstOrNull()?.id ?: "guru1") }
        var teacherName by remember { mutableStateOf(teachers.firstOrNull()?.name ?: "Budi Santoso, S.Pd.") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Rombongan Kelas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("ID Kelas (e.g., 10-A)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Kelas Visual (e.g., Kelas X-A)") }, modifier = Modifier.fillMaxWidth())
                    
                    Text("Pilih Wali Kelas:", style = MaterialTheme.typography.labelMedium)
                    teachers.forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    teacherId = t.id
                                    teacherName = t.name
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = teacherId == t.id, onClick = {
                                teacherId = t.id
                                teacherName = t.name
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(t.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (id.isNotEmpty() && name.isNotEmpty()) {
                            val newCls = ClassEntity(id, name, teacherId, teacherName)
                            viewModel.addOrUpdateClass(newCls)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }
}

// ================= CALENDAR/TODO REMINDERS (ADMIN) =================
@Composable
fun CalendarTodoScreen(viewModel: SchoolViewModel, todos: List<TodoEntity>) {
    var showAddDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("2026-06-07") }
    var timeString by remember { mutableStateOf("08:00") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("todo_calendar_root")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Asisten Google Kalender", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Daftar agenda pengingat administratif", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agenda")
            }
        }

        if (todos.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Tidak ada pengingat kalender.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(todos) { todo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (todo.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = todo.isCompleted,
                                onCheckedChange = { viewModel.toggletodoCompletion(todo) }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = todo.title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (todo.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    todo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (todo.isCompleted) Color.LightGray else Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${todo.date} @ ${todo.time}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            IconButton(onClick = { viewModel.removeTodo(todo.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Agenda Pengingat Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Kegiatan") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dateString, onValueChange = { dateString = it }, label = { Text("Tanggal (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = timeString, onValueChange = { timeString = it }, label = { Text("Jam (HH:MM)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.addCalendarTodo(title, description, dateString, timeString)
                            title = ""
                            description = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Pasang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }
}
