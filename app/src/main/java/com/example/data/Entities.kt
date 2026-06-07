package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String, // "ADMIN", "GURU", "ORANG_TUA"
    val email: String,
    val profilePic: String = "", // Holds base64 or URI pattern
    val childrenIds: String = "" // Comma-separated Student IDS for Parent accounts
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nisn: String,
    val classId: String,
    val parentId: String, // Associates with UserEntity of role ORANG_TUA
    val parentName: String = "",
    val birthDate: String = "2010-01-01",
    val birthPlace: String = "Jakarta",
    val address: String = "Jl. Merdeka No. 12",
    val academicHistory: String = "IPK: 3.8, Absensi: 98%"
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nip: String,
    val subject: String,
    val phone: String = ""
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey val id: String,
    val name: String,
    val teacherId: String,
    val teacherName: String = ""
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val authorName: String,
    val authorRole: String, // "ADMIN" or "GURU"
    val targetRole: String, // "ALL", "GURU", "ORANG_TUA"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val studentName: String,
    val classId: String,
    val date: String, // "YYYY-MM-DD"
    val status: String, // "Hadir", "Sakit", "Izin", "Alpa"
    val info: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "class_journals")
data class ClassJournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classId: String,
    val className: String,
    val date: String, // "YYYY-MM-DD"
    val subject: String,
    val topic: String,
    val notes: String,
    val teacherName: String
)

@Entity(tableName = "journal_history")
data class JournalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val journalId: Int,
    val className: String,
    val editedBy: String,
    val role: String,
    val editTimestamp: Long = System.currentTimeMillis(),
    val actionType: String, // "CREATE" or "EDIT"
    val previousTopic: String = "",
    val currentTopic: String = "",
    val previousNotes: String = "",
    val currentNotes: String = ""
)

@Entity(tableName = "ebooks")
data class EbookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val category: String,
    val size: String = "2.4 MB",
    val description: String = "",
    val fileUriOrPath: String = "", // Standard mock path
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classId: String,
    val className: String,
    val subject: String,
    val examName: String, // "Ujian Tengah Semester", "Ujian Akhir Semester" etc.
    val date: String, // "YYYY-MM-DD"
    val time: String, // "08:00 - 10:00"
    val maxScore: Int = 100
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int,
    val examName: String,
    val subject: String,
    val studentId: String,
    val studentName: String,
    val score: Double,
    val notes: String = ""
)

@Entity(tableName = "activity_media")
data class ActivityMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val classId: String,
    val className: String,
    val studentId: String = "", // Optional reference
    val studentName: String = "",
    val mediaUri: String, // base64 visual resource or drawable identification
    val isVideo: Boolean = false,
    val uploadedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ahlak_monitors")
data class AhlakMonitorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val category: String, // e.g., "Sholat Berjamaah", "Membaca Al-Qur'an", "Sopan Santun", "Membantu Orang Tua"
    val status: String, // "Sangat Baik", "Baik", "Cukup", "Perlu Perbaikan"
    val lastCheckedDate: String, // "YYYY-MM-DD"
    val note: String = ""
)

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String, // "YYYY-MM-DD"
    val time: String = "08:00",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val targetRole: String, // "ALL", "GURU", "ORANG_TUA"
    val relatedId: String = "", // student ID or general ID
    val category: String = "GENERAL", // "ATTENDANCE", "ANNOUNCEMENT", "EXAM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "character_traits_config")
data class CharacterTraitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = ""
)
