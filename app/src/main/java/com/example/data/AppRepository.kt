package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(private val schoolDao: SchoolDao) {

    // --- REALTIME SYNC STATUS ---
    private val _syncStatus = MutableStateFlow<SyncState>(SyncState.Synced)
    val syncStatus: StateFlow<SyncState> = _syncStatus.asStateFlow()

    enum class SyncState {
        Synced, Synchronizing, Offline, Disconnected
    }

    // --- DATA REFLOWS ---
    val allUsers: Flow<List<UserEntity>> = schoolDao.getAllUsers()
    val allStudents: Flow<List<StudentEntity>> = schoolDao.getAllStudents()
    val allTeachers: Flow<List<TeacherEntity>> = schoolDao.getAllTeachers()
    val allClasses: Flow<List<ClassEntity>> = schoolDao.getAllClasses()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = schoolDao.getAllAnnouncements()
    val allAttendance: Flow<List<AttendanceEntity>> = schoolDao.getAllAttendance()
    val allJournals: Flow<List<ClassJournalEntity>> = schoolDao.getAllJournals()
    val allJournalHistory: Flow<List<JournalHistoryEntity>> = schoolDao.getAllJournalHistory()
    val allEbooks: Flow<List<EbookEntity>> = schoolDao.getAllEbooks()
    val allExams: Flow<List<ExamEntity>> = schoolDao.getAllExams()
    val allExamResults: Flow<List<ExamResultEntity>> = schoolDao.getAllExamResults()
    val allActivityMedia: Flow<List<ActivityMediaEntity>> = schoolDao.getAllActivityMedia()
    val allAhlakMonitors: Flow<List<AhlakMonitorEntity>> = schoolDao.getAllAhlakMonitors()
    val allCharacterTraits: Flow<List<CharacterTraitEntity>> = schoolDao.getAllCharacterTraits()
    val allTodos: Flow<List<TodoEntity>> = schoolDao.getAllTodos()
    val allNotifications: Flow<List<NotificationEntity>> = schoolDao.getAllNotifications()

    fun getStudentsByIds(ids: List<String>): Flow<List<StudentEntity>> = schoolDao.getStudentsByIds(ids)
    fun getStudentsByClass(classId: String): Flow<List<StudentEntity>> = schoolDao.getStudentsByClass(classId)
    fun getAhlakMonitorsByStudent(studentId: String): Flow<List<AhlakMonitorEntity>> = schoolDao.getAhlakMonitorsByStudent(studentId)
    fun getAttendanceByStudent(studentId: String): Flow<List<AttendanceEntity>> = schoolDao.getAttendanceByStudent(studentId)
    fun getExamResultsByStudent(studentId: String): Flow<List<ExamResultEntity>> = schoolDao.getExamResultsByStudent(studentId)

    // --- RETRIEVERS ---
    suspend fun getUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        schoolDao.getUserById(userId)
    }

    suspend fun getStudentById(studentId: String): StudentEntity? = withContext(Dispatchers.IO) {
        schoolDao.getStudentById(studentId)
    }

    suspend fun getJournalById(journalId: Int): ClassJournalEntity? = withContext(Dispatchers.IO) {
        schoolDao.getJournalById(journalId)
    }

    // --- WRITERS ---
    suspend fun updateUserProfile(user: UserEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertUser(user)
    }

    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertUser(user)
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        schoolDao.deleteUserById(userId)
    }

    suspend fun insertStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertStudent(student)
        // Auto-seed initial custom behavioral values (Ahlak monitoring indicators) for new students
        val defaultAhlakCategories = listOf("Sholat Berjamaah", "Membaca Al-Qur'an", "Sopan Santun", "Membantu Orang Tua", "Kedisiplinan")
        for (category in defaultAhlakCategories) {
            schoolDao.insertAhlakMonitor(
                AhlakMonitorEntity(
                    studentId = student.id,
                    category = category,
                    status = "Baik",
                    lastCheckedDate = "2026-06-07",
                    note = "Dibuat otomatis saat pendaftaran siswa"
                )
            )
        }
    }

    suspend fun updateStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        schoolDao.updateStudent(student)
    }

    suspend fun deleteStudent(studentId: String) = withContext(Dispatchers.IO) {
        schoolDao.deleteStudentById(studentId)
    }

    suspend fun insertTeacher(teacher: TeacherEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertTeacher(teacher)
    }

    suspend fun deleteTeacher(teacherId: String) = withContext(Dispatchers.IO) {
        schoolDao.deleteTeacherById(teacherId)
    }

    suspend fun insertClass(classEntity: ClassEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertClass(classEntity)
    }

    suspend fun deleteClass(classId: String) = withContext(Dispatchers.IO) {
        schoolDao.deleteClassById(classId)
    }

    // --- BUSY INTEGRATION EVENTS ---
    suspend fun insertAnnouncement(announcement: AnnouncementEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertAnnouncement(announcement)
        // Generate simulated Push Notification
        schoolDao.insertNotification(
            NotificationEntity(
                title = "Pengumuman Baru: ${announcement.title}",
                content = announcement.content,
                targetRole = announcement.targetRole,
                category = "ANNOUNCEMENT"
            )
        )
    }

    suspend fun recordAttendance(attendanceList: List<AttendanceEntity>) = withContext(Dispatchers.IO) {
        for (record in attendanceList) {
            val existing = schoolDao.getAttendanceForStudentOnDate(record.studentId, record.date)
            if (existing != null) {
                schoolDao.insertAttendance(record.copy(id = existing.id))
            } else {
                schoolDao.insertAttendance(record)
            }

            // Immediately send virtual push notification to update parents dynamically
            val student = schoolDao.getStudentById(record.studentId)
            if (student != null) {
                schoolDao.insertNotification(
                    NotificationEntity(
                        title = "Kehadiran: ${student.name}",
                        content = "Status kehadiran pada ${record.date} tercatat: ${record.status}${if (record.info.isNotEmpty()) " (${record.info})" else ""}",
                        targetRole = "ORANG_TUA",
                        relatedId = student.id,
                        category = "ATTENDANCE"
                    )
                )
            }
        }
    }

    suspend fun submitClassJournal(classId: String, className: String, date: String, subject: String, topic: String, notes: String, teacherName: String) = withContext(Dispatchers.IO) {
        val journal = ClassJournalEntity(
            classId = classId,
            className = className,
            date = date,
            subject = subject,
            topic = topic,
            notes = notes,
            teacherName = teacherName
        )
        val journalId = schoolDao.insertJournal(journal).toInt()
        
        schoolDao.insertJournalHistory(
            JournalHistoryEntity(
                journalId = journalId,
                className = className,
                editedBy = teacherName,
                role = "GURU",
                actionType = "CREATE",
                currentTopic = topic,
                currentNotes = notes
            )
        )
    }

    suspend fun updateClassJournal(updatedJournal: ClassJournalEntity, editedBy: String, editorRole: String) = withContext(Dispatchers.IO) {
        val oldJournal = schoolDao.getJournalById(updatedJournal.id)
        schoolDao.updateJournal(updatedJournal)

        schoolDao.insertJournalHistory(
            JournalHistoryEntity(
                journalId = updatedJournal.id,
                className = updatedJournal.className,
                editedBy = editedBy,
                role = editorRole,
                actionType = "EDIT",
                previousTopic = oldJournal?.topic ?: "",
                currentTopic = updatedJournal.topic,
                previousNotes = oldJournal?.notes ?: "",
                currentNotes = updatedJournal.notes
            )
        )
    }

    suspend fun insertEbook(ebook: EbookEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertEbook(ebook)
    }

    suspend fun deleteEbook(ebookId: Int) = withContext(Dispatchers.IO) {
        schoolDao.deleteEbookById(ebookId)
    }

    suspend fun createExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertExam(exam)
        // Virtual push notification for exam reminder
        schoolDao.insertNotification(
            NotificationEntity(
                title = "Jadwal Ujian Baru: ${exam.subject}",
                content = "${exam.examName} dijadwalkan pada ${exam.date} jam ${exam.time} untuk ${exam.className}.",
                targetRole = "ALL",
                category = "EXAM"
            )
        )
    }

    suspend fun addExamResult(result: ExamResultEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertExamResult(result)
        // Create notifications for parent to check report instantly
        val student = schoolDao.getStudentById(result.studentId)
        if (student != null) {
            schoolDao.insertNotification(
                NotificationEntity(
                    title = "Hasil Ujian: ${student.name}",
                    content = "Hasil ${result.examName} mata pelajaran ${result.subject} telah keluar dengan nilai: ${result.score}.",
                    targetRole = "ORANG_TUA",
                    relatedId = student.id,
                    category = "EXAM"
                )
            )
        }
    }

    suspend fun uploadActivityMedia(media: ActivityMediaEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertActivityMedia(media)
        schoolDao.insertNotification(
            NotificationEntity(
                title = "Media Kegiatan Baru",
                content = "${media.uploadedBy} mengunggah dokumentasi di ${media.className}: ${media.title}.",
                targetRole = "ORANG_TUA",
                relatedId = media.studentId,
                category = "ANNOUNCEMENT"
            )
        )
    }

    suspend fun updateAhlakMonitor(ahlak: AhlakMonitorEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertAhlakMonitor(ahlak)
    }

    suspend fun insertCharacterTrait(trait: CharacterTraitEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertCharacterTrait(trait)
    }

    suspend fun deleteCharacterTrait(id: String) = withContext(Dispatchers.IO) {
        schoolDao.deleteCharacterTraitById(id)
    }

    suspend fun saveTodo(todo: TodoEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertTodo(todo)
    }

    suspend fun updateTodoStatus(todo: TodoEntity, completed: Boolean) = withContext(Dispatchers.IO) {
        schoolDao.updateTodo(todo.copy(isCompleted = completed))
    }

    suspend fun deleteTodo(todoId: Int) = withContext(Dispatchers.IO) {
        schoolDao.deleteTodoById(todoId)
    }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        schoolDao.markNotificationAsRead(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        schoolDao.markAllNotificationsAsRead()
    }

    // --- SIMULATED REALTIME OFFLINE/ONLINE SYNC ---
    suspend fun performManualSync() = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncState.Synchronizing
        kotlinx.coroutines.delay(1800) // Beautiful simulated network roundtrip and synchronization latency
        _syncStatus.value = SyncState.Synced
        
        // Add a sync notice notification
        schoolDao.insertNotification(
            NotificationEntity(
                title = "Sinkronisasi Berhasil",
                content = "Semua data lokal telah berhasil disinkronkan dengan server pusat secara real-time.",
                targetRole = "ALL",
                category = "GENERAL"
            )
        )
    }

    // --- INTEGRATED MONTHLY AUTO-REPORT GENERATOR (API CALL SIMULATOR) ---
    suspend fun generateMonthlyReport(studentId: String): String = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncState.Synchronizing
        kotlinx.coroutines.delay(1200)
        _syncStatus.value = SyncState.Synced

        val student = schoolDao.getStudentById(studentId)
        val name = student?.name ?: "Siswa"
        
        val reportMessage = """
            --- LAPORAN BULANAN AKADEMIK ---
            Nama Siswa: $name
            NISN: ${student?.nisn}
            Kelas: ${student?.classId}
            Absensi: Presensi 98%, Sakit 1, Alpa 0
            Perkembangan Karakter (Akhlak): Sangat Baik
            Catatan Wali Kelas: Siswa menunjukkan minat yang sangat kuat pada mata pelajaran eksakta. Hubungan sosial dengan teman sebaya sangat baik dan suka menolong.
            Integrasi API: Dikirim otomatis ke perangkat Wali Murid rickyzetes7@gmail.com
        """.trimIndent()

        // Create a parent-facing push notification indicating the reporting invoice
        schoolDao.insertNotification(
            NotificationEntity(
                title = "Laporan Bulanan Terkirim",
                content = "Laporan bulanan untuk $name telah digenerate secara otomatis dan didistribusikan ke perangkat wali murid.",
                targetRole = "ORANG_TUA",
                relatedId = studentId,
                category = "GENERAL"
            )
        )

        return@withContext reportMessage
    }

    // --- PRE-POPULATION / CORE DATABASE SEEDING ---
    suspend fun seedMockDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val users = schoolDao.getAllUsers()
        // Wait, since we are returning Flow, we can query safely with empty check
        val existingUsers = schoolDao.getUserById("admin")
        if (existingUsers == null) {
            Log.d("EduPortal", "Seeding database with default interactive roles...")
            
            // 1. Initial Users
            schoolDao.insertUser(UserEntity("admin", "Sudarsono, M.Pd.", "ADMIN", "admin@sekolah.com", ""))
            schoolDao.insertUser(UserEntity("guru1", "Budi Santoso, S.Pd.", "GURU", "budi@sekolah.com", ""))
            schoolDao.insertUser(UserEntity("ortu1", "Siti Rahma, S.E.", "ORANG_TUA", "rickyzetes7@gmail.com", "", "siswa1,siswa2"))

            // 2. Kids (multi-child parenthood for ortu1)
            schoolDao.insertStudent(StudentEntity("siswa1", "Rizky Santoso", "1029381029", "10-A", "ortu1", "Siti Rahma", "2010-05-12", "Bandung", "Jl. Diponegoro No. 45", "IPK Bulanan: 3.85 | Konsisten"))
            schoolDao.insertStudent(StudentEntity("siswa2", "Alia Santoso", "1029381030", "11-B", "ortu1", "Siti Rahma", "2009-08-24", "Bandung", "Jl. Diponegoro No. 45", "IPK Bulanan: 3.72 | Semangat belajar tinggi"))

            // Extra students
            schoolDao.insertStudent(StudentEntity("siswa3", "Bambang Pamungkas", "1029381031", "10-A", "ortu2", "Joko Widodo"))

            // 3. Classes
            schoolDao.insertClass(ClassEntity("10-A", "Kelas X-A", "guru1", "Budi Santoso, S.Pd."))
            schoolDao.insertClass(ClassEntity("11-B", "Kelas XI-B", "guru1", "Budi Santoso, S.Pd."))

            // 4. Teachers
            schoolDao.insertTeacher(TeacherEntity("guru1", "Budi Santoso, S.Pd.", "198712122010121002", "Matematika", "081234567890"))
            schoolDao.insertTeacher(TeacherEntity("guru2", "Amanda Putri, M.Pd.", "199104042014022001", "Bahasa Inggris", "082345678901"))

            // 5. Announcements
            schoolDao.insertAnnouncement(AnnouncementEntity(0, "Ujian Tengah Semester Ganjil", "Bagi seluruh siswa, UTS Ganjil direncanakan mulai hari Senin depan pukul 07.30 WIB. Kartu ujian harap dibawa.", "Admin", "ADMIN", "ALL"))
            schoolDao.insertAnnouncement(AnnouncementEntity(0, "Kompetisi Sains Nasional (KSN)", "Pendaftaran bimbingan KSN bidang astronomi dan kebumian resmi dibuka di laboratorium fisika.", "Budi Santoso, S.Pd.", "GURU", "ALL"))
            schoolDao.insertAnnouncement(AnnouncementEntity(0, "Penyusunan Rapor Siswa", "Kepada bapak/ibu guru untuk mulai menginput nilai harian dan tugas di jurnal sistem sebelum rapat pleno.", "Admin", "ADMIN", "GURU"))

            // 6. Pre-filled attendance records
            schoolDao.insertAttendance(AttendanceEntity(0, "siswa1", "Rizky Santoso", "10-A", "2026-06-05", "Hadir", "", System.currentTimeMillis() - 86400000))
            schoolDao.insertAttendance(AttendanceEntity(0, "siswa2", "Alia Santoso", "11-B", "2026-06-05", "Izin", "Acara keluarga", System.currentTimeMillis() - 86400000))
            schoolDao.insertAttendance(AttendanceEntity(0, "siswa1", "Rizky Santoso", "10-A", "2026-06-06", "Hadir", "", System.currentTimeMillis() - 40000000))
            schoolDao.insertAttendance(AttendanceEntity(0, "siswa2", "Alia Santoso", "11-B", "2026-06-06", "Hadir", "", System.currentTimeMillis() - 40000000))

            // 7. Base Class Journals & History
            val jId = schoolDao.insertJournal(ClassJournalEntity(0, "10-A", "Kelas X-A", "2026-06-05", "Matematika", "Trigonometri Dasar", "Sifat-sifat sinus dan cosinus.", "Budi Santoso, S.Pd.")).toInt()
            schoolDao.insertJournalHistory(
                JournalHistoryEntity(
                    journalId = jId,
                    className = "Kelas X-A",
                    editedBy = "Budi Santoso, S.Pd.",
                    role = "GURU",
                    editTimestamp = System.currentTimeMillis() - 80000000,
                    actionType = "CREATE",
                    currentTopic = "Trigonometri Dasar",
                    currentNotes = "Sifat-sifat sinus dan cosinus."
                )
            )

            // 8. Ebooks
            schoolDao.insertEbook(EbookEntity(0, "Matematika Kelas X Kurikulum Merdeka", "Kementerian Pendidikan", "Matematika", "8.5 MB", "Buku pegangan utama matematika siswa kelas X kurikulum merdeka nasional."))
            schoolDao.insertEbook(EbookEntity(0, "Kumpulan Soal Fisika Dasar", "Prof. Dr. Suryadi", "Fisika", "4.2 MB", "Buku pembahasan soal intensif olimpiade fisika SMA."))
            schoolDao.insertEbook(EbookEntity(0, "Bahasa Inggris Komprehensif", "Amanda Putri, M.Pd.", "Bahasa Inggris", "12.1 MB", "Buku saku tata bahasa dan fungsional percakapan."))

            // 9. Exams
            val examId1 = schoolDao.insertExam(ExamEntity(0, "10-A", "Kelas X-A", "Matematika", "Kuis Aljabar Linear", "2026-06-10", "08:00 - 09:30")).toInt()
            val examId2 = schoolDao.insertExam(ExamEntity(0, "11-B", "Kelas XI-B", "Bahasa Inggris", "Presentasi Percakapan", "2026-06-11", "10:00 - 11:30")).toInt()

            // 10. Exam Results
            schoolDao.insertExamResult(ExamResultEntity(0, examId1, "Kuis Aljabar Linear", "Matematika", "siswa1", "Rizky Santoso", 95.0, "Hasil sangat memuaskan, pertahankan!"))
            schoolDao.insertExamResult(ExamResultEntity(0, examId2, "Presentasi Percakapan", "Bahasa Inggris", "siswa2", "Alia Santoso", 88.5, "Pengucapan (pronunciation) sangat baik dan fasih."))

            // 11. Activity Media
            schoolDao.insertActivityMedia(ActivityMediaEntity(0, "Praktikum Kimia Asam Basa", "10-A", "Kelas X-A", "siswa1", "Rizky Santoso", "https://picsum.photos/id/101/600/400", false, "Budi Santoso, S.Pd."))
            schoolDao.insertActivityMedia(ActivityMediaEntity(0, "Presentasi Kelompok Bahasa Inggris", "11-B", "Kelas XI-B", "siswa2", "Alia Santoso", "https://picsum.photos/id/102/600/400", false, "Amanda Putri, M.Pd."))

            // 12. Ahlak Monitors Default
            schoolDao.insertAhlakMonitor(AhlakMonitorEntity(0, "siswa1", "Sholat Berjamaah", "Sangat Baik", "2026-06-06", "Konsisten sholat dhuha dan dhuhur berjamaah."))
            schoolDao.insertAhlakMonitor(AhlakMonitorEntity(0, "siswa1", "Membaca Al-Qur'an", "Baik", "2026-06-06", "Membaca surah Al-Kahfi setiap hari jumat."))
            schoolDao.insertAhlakMonitor(AhlakMonitorEntity(0, "siswa1", "Sopan Santun", "Sangat Baik", "2026-06-06", "Sangat menghormati guru dan menyayangi sesama teman."))
            schoolDao.insertAhlakMonitor(AhlakMonitorEntity(0, "siswa2", "Sholat Berjamaah", "Baik", "2026-06-06", "Sholat berjamaah tepat waktu."))
            schoolDao.insertAhlakMonitor(AhlakMonitorEntity(0, "siswa2", "Membantu Orang Tua", "Sangat Baik", "2026-06-06", "Membantu membereskan kelas dan keperluan lab."))

            // 13. System Todos
            schoolDao.insertTodo(TodoEntity(0, "Verifikasi Berkas Raport Semester", "Memeriksa tanda tangan kepala sekolah sebelum pendistribusian raport digital.", "2026-06-07", "09:00", false))
            schoolDao.insertTodo(TodoEntity(0, "Rapat Koordinasi Ujian Akhir", "Diskusi panitia ujian guru mengenai pengawas cadangan.", "2026-06-09", "13:00", false))

            // 14. Seed Notifications
            schoolDao.insertNotification(NotificationEntity(0, "Sistem Siap Digunakan", "Selamat datang di portal akademik EduPortal. Sistem berjalan dengan mode offline-first yang handal.", "ALL", category = "GENERAL"))

            // 15. Seed Character Traits Config
            schoolDao.insertCharacterTrait(CharacterTraitEntity("sholat", "Sholat Berjamaah", "Kekonsistenan siswa dalam sholat fardhu berjamaah di masjid/mushola."))
            schoolDao.insertCharacterTrait(CharacterTraitEntity("alquran", "Membaca Al-Qur'an", "Kekonsistenan siswa dalam membaca (tadarus) Al-Qur'an."))
            schoolDao.insertCharacterTrait(CharacterTraitEntity("sopan", "Sopan Santun", "Sikap santun dalam bertanding ucapan dan perilaku kepada guru, kakak kelas, dan kawan sejawat."))
            schoolDao.insertCharacterTrait(CharacterTraitEntity("ortu", "Membantu Orang Tua", "Tingkat kesadaran moral membantu orang tua di luar jam sekolah."))
            schoolDao.insertCharacterTrait(CharacterTraitEntity("disiplin", "Kedisiplinan", "Ketepatan waktu mengumpulkan tugas akademik, mentaati aturan seragam, dan tata tertib."))
            
            Log.d("EduPortal", "Database seeding finished successfully.")
        }
    }
}
