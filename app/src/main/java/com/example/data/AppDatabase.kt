package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    // --- USER ACTIONS ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    // --- STUDENT ACTIONS ---
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE classId = :classId")
    fun getStudentsByClass(classId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id IN (:ids)")
    fun getStudentsByIds(ids: List<String>): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: String)

    // --- TEACHER ACTIONS ---
    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    @Query("DELETE FROM teachers WHERE id = :teacherId")
    suspend fun deleteTeacherById(teacherId: String)

    // --- CLASS ACTIONS ---
    @Query("SELECT * FROM classes")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Query("DELETE FROM classes WHERE id = :classId")
    suspend fun deleteClassById(classId: String)

    // --- ANNOUNCEMENTS ---
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    // --- ATTENDANCE ---
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classId = :classId ORDER BY date DESC")
    fun getAttendanceByClass(classId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceByStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getAttendanceForStudentOnDate(studentId: String, date: String): AttendanceEntity?

    // --- JOURNALS & HISTORY ---
    @Query("SELECT * FROM class_journals ORDER BY date DESC")
    fun getAllJournals(): Flow<List<ClassJournalEntity>>

    @Query("SELECT * FROM class_journals WHERE id = :journalId")
    suspend fun getJournalById(journalId: Int): ClassJournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: ClassJournalEntity): Long

    @Update
    suspend fun updateJournal(journal: ClassJournalEntity)

    @Query("SELECT * FROM journal_history ORDER BY editTimestamp DESC")
    fun getAllJournalHistory(): Flow<List<JournalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalHistory(history: JournalHistoryEntity)

    // --- EBOOKS ---
    @Query("SELECT * FROM ebooks ORDER BY timestamp DESC")
    fun getAllEbooks(): Flow<List<EbookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEbook(ebook: EbookEntity)

    @Query("DELETE FROM ebooks WHERE id = :ebookId")
    suspend fun deleteEbookById(ebookId: Int)

    // --- EXAMS & EXAM RESULTS ---
    @Query("SELECT * FROM exams ORDER BY date DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE classId = :classId ORDER BY date DESC")
    fun getExamsByClass(classId: String): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Query("SELECT * FROM exam_results")
    fun getAllExamResults(): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getExamResultsByStudent(studentId: String): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity)

    // --- ACTIVITY MEDIA (PHOTOS/VIDEOS) ---
    @Query("SELECT * FROM activity_media ORDER BY timestamp DESC")
    fun getAllActivityMedia(): Flow<List<ActivityMediaEntity>>

    @Query("SELECT * FROM activity_media WHERE classId = :classId ORDER BY timestamp DESC")
    fun getActivityMediaByClass(classId: String): Flow<List<ActivityMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityMedia(media: ActivityMediaEntity)

    // --- AHLAK MONITORING ---
    @Query("SELECT * FROM ahlak_monitors")
    fun getAllAhlakMonitors(): Flow<List<AhlakMonitorEntity>>

    @Query("SELECT * FROM ahlak_monitors WHERE studentId = :studentId")
    fun getAhlakMonitorsByStudent(studentId: String): Flow<List<AhlakMonitorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAhlakMonitor(ahlak: AhlakMonitorEntity)

    // --- CHARACTER TRAITS CONFIG ---
    @Query("SELECT * FROM character_traits_config")
    fun getAllCharacterTraits(): Flow<List<CharacterTraitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterTrait(trait: CharacterTraitEntity)

    @Query("DELETE FROM character_traits_config WHERE id = :id")
    suspend fun deleteCharacterTraitById(id: String)

    // --- CONTEXT REMINDERS / TODOS ---
    @Query("SELECT * FROM todos ORDER BY date DESC, time DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :todoId")
    suspend fun deleteTodoById(todoId: Int)

    // --- LOCAL PUSH NOTIFICATIONS ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()
}

@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        TeacherEntity::class,
        ClassEntity::class,
        AnnouncementEntity::class,
        AttendanceEntity::class,
        ClassJournalEntity::class,
        JournalHistoryEntity::class,
        EbookEntity::class,
        ExamEntity::class,
        ExamResultEntity::class,
        ActivityMediaEntity::class,
        AhlakMonitorEntity::class,
        TodoEntity::class,
        NotificationEntity::class,
        CharacterTraitEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edu_portal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
