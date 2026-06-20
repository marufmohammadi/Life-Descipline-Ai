package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 'current_user' LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 'current_user' LIMIT 1")
    suspend fun getUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate DESC, timeOfDay ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date")
    fun getTasksForDateFlow(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabitsFlow(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines")
    fun getAllRoutinesFlow(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY targetDate ASC")
    fun getAllGoalsFlow(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)
}

@Dao
interface ScoreHistoryDao {
    @Query("SELECT * FROM score_history ORDER BY date ASC")
    fun getAllScoreHistoryFlow(): Flow<List<ScoreHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreHistoryEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesSync(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedAchievements(): List<AchievementEntity>

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET clicked = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterLogForDateFlow(date: String): Flow<WaterLogEntity?>

    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    suspend fun getWaterLogForDateSync(date: String): WaterLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity)
}

@Dao
interface SleepLogDao {
    @Query("SELECT * FROM sleep_logs WHERE date = :date LIMIT 1")
    fun getSleepLogForDateFlow(date: String): Flow<SleepLogEntity?>

    @Query("SELECT * FROM sleep_logs WHERE date = :date LIMIT 1")
    suspend fun getSleepLogForDateSync(date: String): SleepLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLogEntity)
}

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        RoutineEntity::class,
        GoalEntity::class,
        ScoreHistoryEntity::class,
        ChatMessageEntity::class,
        AchievementEntity::class,
        NotificationEntity::class,
        WaterLogEntity::class,
        SleepLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun routineDao(): RoutineDao
    abstract fun goalDao(): GoalDao
    abstract fun scoreHistoryDao(): ScoreHistoryDao
    abstract fun chatDao(): ChatDao
    abstract fun achievementDao(): AchievementDao
    abstract fun notificationDao(): NotificationDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun sleepLogDao(): SleepLogDao
}
