package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.SchoolViewModel

// ================= MONITORING AKHLAK / CHARACTER PROGRESS (ORANG TUA) =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AkhlakScreen(
    viewModel: SchoolViewModel,
    currentUser: UserEntity,
    students: List<StudentEntity>,
    monitors: List<AhlakMonitorEntity>,
    activeChildId: String,
    traits: List<CharacterTraitEntity>
) {
    if (currentUser.role == "ADMIN") {
        // --- ADMIN: MANAGE CONFIGURABLE TRAITS ---
        var newTraitName by remember { mutableStateOf("") }
        var newTraitDesc by remember { mutableStateOf("") }
        var isSavedByAdmin by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("akhlak_admin_root")) {
            Text(
                text = "Konfigurasi Indikator Karakter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Atur kriteria dan indikator moral/perilaku yang wajib dipantau oleh wali murid.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add New Trait Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tambah Indikator Karakter Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = newTraitName,
                        onValueChange = { newTraitName = it },
                        label = { Text("Nama Karakter / Perilaku") },
                        placeholder = { Text("e.g., Kejujuran Akademik") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newTraitDesc,
                        onValueChange = { newTraitDesc = it },
                        label = { Text("Deskripsi / Petunjuk Pengisian") },
                        placeholder = { Text("e.g., Selalu jujur saat ujian, mengakui kesalahan...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Button(
                        onClick = {
                            if (newTraitName.isNotEmpty()) {
                                val traitId = newTraitName.lowercase(Locale.ROOT).replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                viewModel.saveCharacterTrait(
                                    CharacterTraitEntity(
                                        id = traitId,
                                        name = newTraitName,
                                        description = newTraitDesc
                                    )
                                )
                                newTraitName = ""
                                newTraitDesc = ""
                                isSavedByAdmin = true
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Karakter")
                    }
                }
            }

            if (isSavedByAdmin) {
                Text(
                    text = "Karakter baru berhasil dikonfigurasi & disebarkan!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Indikator Terdaftar saat ini (${traits.size}):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (traits.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada indikator terdaftar.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(traits) { trait ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = trait.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (trait.description.isNotEmpty()) {
                                        Text(
                                            text = trait.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteCharacterTrait(trait.id) }
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- PARENTS & TEACHERS: MONITOR AND RECORD OBSERVATIONS ---
        val parentKidIds = currentUser.childrenIds.split(",").filter { it.isNotEmpty() }
        val myKids = students.filter { parentKidIds.contains(it.id) || currentUser.role == "GURU" }
        val selectedChild = students.find { it.id == activeChildId } ?: myKids.firstOrNull()

        val childMonitors = selectedChild?.let { kid ->
            monitors.filter { it.studentId == kid.id }
        } ?: emptyList()

        var showAddObservationDialog by remember { mutableStateOf(false) }
        var showEditIndicatorDialog by remember { mutableStateOf<AhlakMonitorEntity?>(null) }
        
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("akhlak_root")) {
            Text(
                text = "Pemantauan Karakter & Akhlak",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Wali murid dan Guru bekerjasama mencatat & memantau indikator karakter unggulan sekolah.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Child Picker / Selector
            if (myKids.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    items(myKids) { kid ->
                        val isSelected = kid.id == activeChildId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setActiveChild(kid.id) },
                            label = { Text(kid.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            selectedChild?.let { kid ->
                // Banner Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolunteerActivism, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Siswa terpilih: ${kid.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Gunakan tombol Observasi di bawah untuk melaporkan perkembangan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jurnal Pengamatan Karakter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddObservationDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Observasi")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (childMonitors.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.TaskAlt, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada pengamatan untuk ${kid.name}.", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(childMonitors) { ahl ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    KeyAkhlakStatusBadge(status = ahl.status)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ahl.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        if (ahl.note.isNotEmpty()) {
                                            Text(ahl.note, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Text(
                                            text = "Terakhir diamati: ${ahl.lastCheckedDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    IconButton(
                                        onClick = { showEditIndicatorDialog = ahl }
                                    ) {
                                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Silahkan login kembali atau pilih anak terdaftar.", color = Color.Gray)
                }
            }
        }

        // --- NEW DIALOG: ADD/RECORD OBSERVABILITY ---
        if (showAddObservationDialog && selectedChild != null) {
            var selectedTrait by remember { mutableStateOf(traits.firstOrNull()?.name ?: "Sopan Santun") }
            var checkedStatus by remember { mutableStateOf("Baik") }
            var newNotes by remember { mutableStateOf("") }
            var dropdownExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAddObservationDialog = false },
                title = { Text("Rekam Pengamatan Baru") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pilih Indikator Karakter:", style = MaterialTheme.typography.labelMedium)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedTrait)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (traits.isEmpty()) {
                                    listOf("Sholat Berjamaah", "Membaca Al-Qur'an", "Sopan Santun", "Membantu Orang Tua", "Kedisiplinan").forEach { traitName ->
                                        DropdownMenuItem(
                                            text = { Text(traitName) },
                                            onClick = {
                                                selectedTrait = traitName
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                } else {
                                    traits.forEach { trait ->
                                        DropdownMenuItem(
                                            text = { Text(trait.name) },
                                            onClick = {
                                                selectedTrait = trait.name
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text("Status Penilaian:", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Sangat Baik", "Baik", "Cukup", "Perlu Perbaikan").forEach { st ->
                                FilterChip(
                                    selected = checkedStatus == st,
                                    onClick = { checkedStatus = st },
                                    label = { Text(st, fontSize = 10.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newNotes,
                            onValueChange = { newNotes = it },
                            label = { Text("Catatan / Deskripsi Kejadian") },
                            placeholder = { Text("E.g. Membantu membersihkan hiasan kelas sebelum pulang.") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val currentDateStr = sdf.format(Date())
                            val observation = AhlakMonitorEntity(
                                studentId = selectedChild.id,
                                category = selectedTrait,
                                status = checkedStatus,
                                lastCheckedDate = currentDateStr,
                                note = newNotes
                            )
                            viewModel.saveAhlakEvaluation(observation)
                            showAddObservationDialog = false
                        }
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddObservationDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // --- DIALOG: EDIT OBSERVILITY ---
        if (showEditIndicatorDialog != null) {
            val ahl = showEditIndicatorDialog!!
            var checkedStatus by remember { mutableStateOf(ahl.status) }
            var notes by remember { mutableStateOf(ahl.note) }

            AlertDialog(
                onDismissRequest = { showEditIndicatorDialog = null },
                title = { Text("Ubah Penilaian Akhlak") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(ahl.category, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        
                        Text("Kategori Penilaian:")
                        listOf("Sangat Baik", "Baik", "Cukup", "Perlu Perbaikan").forEach { st ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { checkedStatus = st }
                            ) {
                                RadioButton(selected = checkedStatus == st, onClick = { checkedStatus = st })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(st)
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Ulasan kriteria khusus") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val currentDateStr = sdf.format(Date())
                            val updated = ahl.copy(status = checkedStatus, note = notes, lastCheckedDate = currentDateStr)
                            viewModel.saveAhlakEvaluation(updated)
                            showEditIndicatorDialog = null
                        }
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditIndicatorDialog = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun KeyAkhlakStatusBadge(status: String) {
    val (color, char) = when (status) {
        "Sangat Baik" -> Color(0xFF4CAF50) to "SB"
        "Baik" -> Color(0xFF2196F3) to "B"
        "Cukup" -> Color(0xFFFF9800) to "C"
        else -> Color(0xFFE51C23) to "PP"
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(char, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
    }
}


// ================= UNIVERSAL EDIT PROFILE CONFIGURATOR =================
@Composable
fun UserProfileScreen(viewModel: SchoolViewModel, currentUser: UserEntity) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf(currentUser.name) }
    var email by remember { mutableStateOf(currentUser.email) }
    var selectedEmoji by remember { mutableStateOf(currentUser.profilePic.ifEmpty { "👨‍🏫" }) }

    val avatarEmojis = listOf("👨‍🏫", "👩‍🏫", "👨‍🎓", "👩‍🎓", "👨‍💼", "👩‍💼", "🦊", "🦁", "🐧")
    var isSavedAlertVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profile_root")) {
        Text("Kelola Profil Pengguna", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Ubah identitas serta foto lambang profil avatar Anda", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Profile Pick Avatar Symbol
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedEmoji, fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Pilih Ikon Avatar Baru Anda:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(avatarEmojis) { em ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (selectedEmoji == em) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedEmoji = em }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(em, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Pengguna") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Alamat Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSavedAlertVisible) {
            Text(
                "Profil berhasil disave harian ke Room DB lokal!",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                viewModel.updateProfile(name, email, selectedEmoji)
                isSavedAlertVisible = true
                coroutineScope.launch {
                    kotlinx.coroutines.delay(2500)
                    isSavedAlertVisible = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_button")
        ) {
            Icon(Icons.Default.Save, null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan Konfigurasi Profil", fontWeight = FontWeight.Bold)
        }
    }
}

// ================= GENERAL SETTINGS AND OFFLINE ACCESS CENTER =================
@Composable
fun SettingsScreen(
    viewModel: SchoolViewModel,
    isDarkTheme: Boolean,
    syncState: com.example.data.AppRepository.SyncState,
    currentUser: UserEntity,
    onRoleSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag("settings_root")) {
        Text("Pengaturan EduPortal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Konfigurasi tema tampilan dan kontrol sinkronisasi offline", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Darkmode
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Mode Tema Gelap", fontWeight = FontWeight.Bold)
                        Text("Hemat daya mata pada pencahayaan rendah", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme() },
                    modifier = Modifier.testTag("darkmode_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Realtime Data Sync Setting Card
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Mode Sinkronisasi Real-time", fontWeight = FontWeight.Bold)
                            Text(
                                "Status: ${if (syncState == com.example.data.AppRepository.SyncState.Synchronizing) "Sedang Mensinkronkan..." else "Siap & Terhubung Server"}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.startDatabaseSync() },
                        enabled = syncState != com.example.data.AppRepository.SyncState.Synchronizing
                    ) {
                        Text("Sync Now", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aplikasi didesain khusus berjalan dengan fungsionalitas 'Offline Access'. Seluruh data tetap tersimpan aman di database SQLite lokal perangkat Anda dan akan disinkronisasikan otomatis ketika internet tersambung kembali.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Role Playground Quick Swapper inside Settings
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Role Switching Playground", fontWeight = FontWeight.Bold)
                Text("Cepat ganti tipe akun sandbox untuk mengevaluasi modul", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRoleSelected("ADMIN") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser.role == "ADMIN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (currentUser.role == "ADMIN") Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Admin", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onRoleSelected("GURU") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser.role == "GURU") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (currentUser.role == "GURU") Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guru", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onRoleSelected("ORANG_TUA") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser.role == "ORANG_TUA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (currentUser.role == "ORANG_TUA") Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Wali", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "EduPortal Versi v1.0.0-Playground\nrickyzetes7@gmail.com\nAI Studio Build Project • Android Native SDK Kotlin",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
