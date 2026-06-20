package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String = "Discipline Aspirant",
    val age: Int = 25,
    val gender: String = "Not Specified",
    val occupation: String = "Student",
    val goals: String = "Build discipline, stay active, maintain routine.",
    val targetWakeTime: String = "06:00 AM",
    val targetSleepTime: String = "10:30 PM",
    val weight: Double = 70.0,
    val height: Double = 175.0,
    val dailyTargetHours: Double = 8.0,
    val profilePhotoPath: String = "avatar_1",
    val isRegistered: Boolean = false,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueDate: String, // YYYY-MM-DD
    val frequency: String, // Daily, Weekly, Monthly
    val category: String, // Workout, Study, Routine, General
    val status: String, // Pending, InProgress, Completed, Missed
    val priority: String, // Low, Medium, High
    val timeOfDay: String, // Morning, Afternoon, Evening
    val isCustom: Boolean = false
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Wake Early, Exercise, Read Books, Meditation, Water Intake, Learning, etc.
    val streakCount: Int = 0,
    val maxStreak: Int = 0,
    val completionHistory: String = "", // Comma-separated YYYY-MM-DD values
    val reminderTime: String = "08:00 AM",
    val isCustom: Boolean = false
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // Morning Routine, Study Routine, Office Routine, etc.
    val iconResName: String = "ic_routine",
    val isActive: Boolean = true,
    val scheduleTime: String = "07:00 AM",
    val priority: String = "Medium",
    val tasksList: String = "Wake Up, Stretch, Drink Water", // Comma-separated routine items
    val reminderEnabled: Boolean = true
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val targetDate: String, // YYYY-MM-DD
    val type: String, // Short-term, Medium-term, Long-term
    val status: String, // Pending, InProgress, Completed, Missed
    val progress: Int = 0 // 0 to 100
)

@Entity(tableName = "score_history")
data class ScoreHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val overallScore: Int,
    val sleepScore: Int,
    val workoutScore: Int,
    val studyScore: Int,
    val routineScore: Int
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // User, AI
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val iconName: String, // Name of the badge
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val clicked: Boolean = false,
    val type: String = "Alert" // Alert, Tip, Custom
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val amountMl: Int,
    val targetMl: Int = 2000
)

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val actualWakeTime: String = "07:00 AM",
    val actualSleepTime: String = "11:00 PM",
    val wakeUpConsistencyScore: Int = 100,
    val sleepConsistencyScore: Int = 100
)
