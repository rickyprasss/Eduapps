package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // --- SCREEN STATE ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- LOGGED-IN STATE & SWITCHING ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // For parent accounts with multiple children
    private val _activeChildId = MutableStateFlow<String>("")
    val activeChildId: StateFlow<String> = _activeChildId.asStateFlow()

    // --- FORM TEMPORARY STATES ---
    private val _selectedClassId = MutableStateFlow<String>("10-A")
    val selectedClassId: StateFlow<String> = _selectedClassId.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // --- REPOSITORY SOURCE FLOWS ---
    val usersList: StateFlow<List<UserEntity>>
    val studentsList: StateFlow<List<StudentEntity>>
    val teachersList: StateFlow<List<TeacherEntity>>
    val classesList: StateFlow<List<ClassEntity>>
    val announcementsList: StateFlow<List<AnnouncementEntity>>
    val attendanceList: StateFlow<List<AttendanceEntity>>
    val journalsList: StateFlow<List<ClassJournalEntity>>
    val journalHistoryList: StateFlow<List<JournalHistoryEntity>>
    val ebooksList: StateFlow<List<EbookEntity>>
    val examsList: StateFlow<List<ExamEntity>>
    val examResultsList: StateFlow<List<ExamResultEntity>>
    val activityMediaList: StateFlow<List<ActivityMediaEntity>>
    val ahlakMonitorsList: StateFlow<List<AhlakMonitorEntity>>
    val characterTraitsList: StateFlow<List<CharacterTraitEntity>>
    val todosList: StateFlow<List<TodoEntity>>
    val notificationsList: StateFlow<List<NotificationEntity>>
    val syncStatus: StateFlow<AppRepository.SyncState>

    sealed class Screen {
        object Dashboard : Screen()
        object DataSiswa : Screen()
        object DataGuru : Screen()
        object DataKelas : Screen()
        object Absensi : Screen()
        object Jurnal : Screen()
        object Ebook : Screen()
        object Ujian : Screen()
        object Media : Screen()
        object Akhlak : Screen()
        object CalendarTodo : Screen()
        object Settings : Screen()
        object Profile : Screen()
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.schoolDao())

        // Compile and map flows from repository to ViewModels
        usersList = repository.allUsers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        studentsList = repository.allStudents.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        teachersList = repository.allTeachers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        classesList = repository.allClasses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        announcementsList = repository.allAnnouncements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        attendanceList = repository.allAttendance.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        journalsList = repository.allJournals.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        journalHistoryList = repository.allJournalHistory.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        ebooksList = repository.allEbooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        examsList = repository.allExams.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        examResultsList = repository.allExamResults.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        activityMediaList = repository.allActivityMedia.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        ahlakMonitorsList = repository.allAhlakMonitors.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        characterTraitsList = repository.allCharacterTraits.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        todosList = repository.allTodos.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        notificationsList = repository.allNotifications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        syncStatus = repository.syncStatus

        // Initialize Seeding and Default Active User in background
        viewModelScope.launch {
            repository.seedMockDatabaseIfEmpty()
            
            // Auto-login to Admin as initial playground role
            val adminUser = repository.getUserById("admin")
            if (adminUser != null) {
                _currentUser.value = adminUser
            }
        }
    }

    // --- NAVIGATION CONTROLLER ---
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- ROLE SWAPPING PLAYGROUND ENGINE ---
    fun switchRoleTo(role: String) {
        viewModelScope.launch {
            val user = when (role) {
                "ADMIN" -> repository.getUserById("admin")
                "GURU" -> repository.getUserById("guru1")
                "ORANG_TUA" -> {
                    val ortu = repository.getUserById("ortu1")
                    // Default to first child when switching to Parent
                    if (ortu != null && ortu.childrenIds.isNotEmpty()) {
                        _activeChildId.value = ortu.childrenIds.split(",").firstOrNull() ?: ""
                    }
                    ortu
                }
                else -> null
            }
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = Screen.Dashboard
            }
        }
    }

    // --- TOGGLE DARKMODE ---
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // --- MULTI-CHILD SELECTION FOR PARENTS ---
    fun setActiveChild(studentId: String) {
        _activeChildId.value = studentId
    }

    // --- CLASS FILTER FOR TEACHERS ---
    fun selectClassId(classId: String) {
        _selectedClassId.value = classId
    }

    // --- ADMIN C.U.D: STUDENTS ---
    fun addOrUpdateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.insertStudent(student)
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
        }
    }

    // --- ADMIN C.U.D: TEACHERS ---
    fun addOrUpdateTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.insertTeacher(teacher)
        }
    }

    fun deleteTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.deleteTeacher(teacherId)
        }
    }

    // --- ADMIN C.U.D: CLASSES ---
    fun addOrUpdateClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            repository.insertClass(classEntity)
        }
    }

    fun deleteClass(classId: String) {
        viewModelScope.launch {
            repository.deleteClass(classId)
        }
    }

    // --- ANNOUNCEMENT ---
    fun postAnnouncement(title: String, content: String, targetRole: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val announcement = AnnouncementEntity(
                title = title,
                content = content,
                authorName = user.name,
                authorRole = user.role,
                targetRole = targetRole
            )
            repository.insertAnnouncement(announcement)
        }
    }

    // --- TEACHER: ATTENDANCE CHECKLIST ---
    fun submitAttendance(studentRecords: List<Pair<StudentEntity, String>>, infoNotes: Map<String, String>) {
        val classId = _selectedClassId.value
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            val attendanceEntities = studentRecords.map { (student, status) ->
                AttendanceEntity(
                    studentId = student.id,
                    studentName = student.name,
                    classId = classId,
                    date = dateString,
                    status = status,
                    info = infoNotes[student.id] ?: ""
                )
            }
            repository.recordAttendance(attendanceEntities)
        }
    }

    // --- TEACHER & ADMIN: CLASS JOURNAL ---
    fun submitClassJournal(classId: String, className: String, subject: String, topic: String, notes: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.submitClassJournal(
                classId = classId,
                className = className,
                date = dateString,
                subject = subject,
                topic = topic,
                notes = notes,
                teacherName = user.name
            )
        }
    }

    fun updateClassJournal(updatedJournal: ClassJournalEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateClassJournal(updatedJournal, user.name, user.role)
        }
    }

    // --- ADMIN: EBOOKS ---
    fun uploadEbook(title: String, author: String, category: String, description: String, fileUri: String = "") {
        viewModelScope.launch {
            val ebook = EbookEntity(
                title = title,
                author = author,
                category = category,
                description = description,
                fileUriOrPath = fileUri.ifEmpty { "MOCK_PATH" }
            )
            repository.insertEbook(ebook)
        }
    }

    fun deleteEbook(ebookId: Int) {
        viewModelScope.launch {
            repository.deleteEbook(ebookId)
        }
    }

    // --- GURU & ADMIN: EXAMS ---
    fun scheduleExam(classId: String, className: String, subject: String, examName: String, date: String, time: String) {
        viewModelScope.launch {
            val exam = ExamEntity(
                classId = classId,
                className = className,
                subject = subject,
                examName = examName,
                date = date,
                time = time
            )
            repository.createExam(exam)
        }
    }

    fun gradeStudentExam(examId: Int, examName: String, subject: String, studentId: String, studentName: String, score: Double, notes: String) {
        viewModelScope.launch {
            val result = ExamResultEntity(
                examId = examId,
                examName = examName,
                subject = subject,
                studentId = studentId,
                studentName = studentName,
                score = score,
                notes = notes
            )
            repository.addExamResult(result)
        }
    }

    // --- ACTIVITY MEDIA (PHOTOS/VIDEOS) ---
    fun uploadMedia(title: String, classId: String, className: String, studentId: String, studentName: String, mediaUri: String, isVideo: Boolean) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val mediaObj = ActivityMediaEntity(
                title = title,
                classId = classId,
                className = className,
                studentId = studentId,
                studentName = studentName,
                mediaUri = mediaUri,
                isVideo = isVideo,
                uploadedBy = user.name
            )
            repository.uploadActivityMedia(mediaObj)
        }
    }

    // --- ORANG TUA: PARENT MONITORING OF CHARACTER (AKHLAK) ---
    fun saveAhlakEvaluation(ahlak: AhlakMonitorEntity) {
        viewModelScope.launch {
            repository.updateAhlakMonitor(ahlak)
        }
    }

    // --- ADMIN & GENERAL REMINDER TODOS (Google Calendar list) ---
    fun addCalendarTodo(title: String, description: String, date: String, time: String) {
        viewModelScope.launch {
            val todo = TodoEntity(
                title = title,
                description = description,
                date = date,
                time = time
            )
            repository.saveTodo(todo)
        }
    }

    fun toggletodoCompletion(todo: TodoEntity) {
        viewModelScope.launch {
            repository.updateTodoStatus(todo, !todo.isCompleted)
        }
    }

    fun removeTodo(todoId: Int) {
        viewModelScope.launch {
            repository.deleteTodo(todoId)
        }
    }

    // --- NOTIFICATIONS MARKING ---
    fun readNotification(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // --- REALTIME ONLINE/OFFLINE SINKRONISASI ---
    fun startDatabaseSync() {
        viewModelScope.launch {
            repository.performManualSync()
        }
    }

    // --- MONTHLY AUTOMATIC API INTEGRATION CALL ---
    private val _lastGeneratedReport = MutableStateFlow<String>("")
    val lastGeneratedReport: StateFlow<String> = _lastGeneratedReport.asStateFlow()

    fun triggerMonthlyReportAPI(studentId: String) {
        viewModelScope.launch {
            val report = repository.generateMonthlyReport(studentId)
            _lastGeneratedReport.value = report
        }
    }

    // --- USER PROFILE UPDATE ---
    fun updateProfile(name: String, email: String, base64Photo: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(
                name = name,
                email = email,
                profilePic = base64Photo
            )
            repository.updateUserProfile(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    // --- CHARACTER TRAITS ADMIN CONFIGURATION ---
    fun saveCharacterTrait(trait: CharacterTraitEntity) {
        viewModelScope.launch {
            repository.insertCharacterTrait(trait)
        }
    }

    fun deleteCharacterTrait(id: String) {
        viewModelScope.launch {
            repository.deleteCharacterTrait(id)
        }
    }
}
