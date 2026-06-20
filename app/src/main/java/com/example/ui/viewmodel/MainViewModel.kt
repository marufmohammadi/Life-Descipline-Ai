package com.example.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.DisciplineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(private val repository: DisciplineRepository) : ViewModel() {

    // --- Core State Streams ---
    val userProfile: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scoreHistory: StateFlow<List<ScoreHistoryEntity>> = repository.allScoreHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Date trackers ---
    val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _currentWaterLog = MutableStateFlow<WaterLogEntity?>(null)
    val currentWaterLog: StateFlow<WaterLogEntity?> = _currentWaterLog.asStateFlow()

    private val _currentSleepLog = MutableStateFlow<SleepLogEntity?>(null)
    val currentSleepLog: StateFlow<SleepLogEntity?> = _currentSleepLog.asStateFlow()

    // --- UI Navigation State ---
    val currentTab = mutableStateOf("Dashboard") // Dashboard, Routines, AI Coach, Focus, Reports, Admin
    val aiIsTyping = mutableStateOf(false)

    // --- Pomodoro Focus Timer State ---
    val focusTimeLeft = mutableStateOf(1500) // 25 min in seconds
    val focusTimerIsRunning = mutableStateOf(false)
    val focusTimerPhase = mutableStateOf("Focus") // Focus, Short Break
    val focusTimerTotalMinutes = mutableStateOf(25)

    // --- Dynamic Score Calculated Values ---
    val currentDisciplineScore = mutableStateOf(85)
    val currentSleepScore = mutableStateOf(90)
    val currentWorkoutScore = mutableStateOf(75)
    val currentStudyScore = mutableStateOf(80)
    val currentRoutineScore = mutableStateOf(85)
    val currentWaterScore = mutableStateOf(70)
    val currentGoalScore = mutableStateOf(90)
    val currentTaskScore = mutableStateOf(85)

    // --- Mood State (Extra feature) ---
    val currentMood = mutableStateOf("Focused") // Focused, Energized, Calmed, Tired, Anxious

    init {
        // Initialize reactive triggers for logging
        viewModelScope.launch {
            repository.getWaterLogForDate(currentDateStr).collect {
                _currentWaterLog.value = it ?: WaterLogEntity(date = currentDateStr, amountMl = 0)
                recalculateDisciplineScores()
            }
        }
        viewModelScope.launch {
            repository.getSleepLogForDate(currentDateStr).collect {
                _currentSleepLog.value = it ?: SleepLogEntity(date = currentDateStr)
                recalculateDisciplineScores()
            }
        }
        viewModelScope.launch {
            // Listen to data adjustments and automatically refresh real-time score statistics
            combine(tasks, habits, goals, _currentWaterLog, _currentSleepLog) { _, _, _, _, _ -> }
                .collect {
                    recalculateDisciplineScores()
                }
        }
        // Auto-seed database if empty on start
        checkAndSeedData()
        startPomodoroClock()
    }

    // --- Score Calculator Core Rule Engine ---
    private fun recalculateDisciplineScores() {
        val todayTasks = tasks.value.filter { it.dueDate == currentDateStr }
        val allHabitList = habits.value
        val allGoalList = goals.value

        // 1. Sleep/Wake score
        val sleepLog = _currentSleepLog.value
        val sleepScoreVal = sleepLog?.sleepConsistencyScore ?: 90
        val wakeScoreVal = sleepLog?.wakeUpConsistencyScore ?: 90
        val finalSleepMetric = (sleepScoreVal + wakeScoreVal) / 2

        // 2. Task Score (today's tasks completion percentage)
        val taskScoreVal = if (todayTasks.isNotEmpty()) {
            val completed = todayTasks.count { it.status == "Completed" }
            (completed.toDouble() / todayTasks.size * 100).toInt()
        } else {
            90 // baseline target default
        }

        // 3. Workout Score (look for any Workout task/habit done today)
        val workoutTasks = todayTasks.filter { it.category == "Workout" }
        val workoutHabits = allHabitList.filter { it.category == "Exercise" }
        val workoutDone = workoutTasks.any { it.status == "Completed" } || 
                workoutHabits.any { isCompletedToday(it, currentDateStr) }
        val workoutScoreVal = if (workoutDone) 100 else 40

        // 4. Study / Learning Score
        val studyTasks = todayTasks.filter { it.category == "Study" }
        val learningHabits = allHabitList.filter { it.category == "Learning" || it.category == "Read Books" }
        val studyDone = studyTasks.any { it.status == "Completed" } ||
                learningHabits.any { isCompletedToday(it, currentDateStr) }
        val studyScoreVal = if (studyDone) 100 else 50

        // 5. Routine Checklist Completion Score
        val routineTasks = todayTasks.filter { it.category == "Routine" }
        val routineScoreVal = if (routineTasks.isNotEmpty()) {
            val completed = routineTasks.count { it.status == "Completed" }
            (completed.toDouble() / routineTasks.size * 100).toInt()
        } else {
            85
        }

        // 6. Water Score
        val waterIntake = _currentWaterLog.value?.amountMl ?: 0
        val targetWater = _currentWaterLog.value?.targetMl ?: 2000
        val waterScoreVal = kotlin.math.min(100, (waterIntake.toDouble() / targetWater * 100).toInt())

        // 7. Goal Score
        val goalScoreVal = if (allGoalList.isNotEmpty()) {
            val completedCount = allGoalList.count { it.status == "Completed" }
            val inProgressCount = allGoalList.count { it.status == "InProgress" }
            val scoreRatio = (completedCount * 1.0 + inProgressCount * 0.5) / allGoalList.size * 100
            scoreRatio.toInt()
        } else {
            100
        }

        // Apply to state variables for UI reactive updates
        currentSleepScore.value = finalSleepMetric
        currentTaskScore.value = taskScoreVal
        currentWorkoutScore.value = workoutScoreVal
        currentStudyScore.value = studyScoreVal
        currentRoutineScore.value = routineScoreVal
        currentWaterScore.value = waterScoreVal
        currentGoalScore.value = goalScoreVal

        val mainDisciplineScore = (finalSleepMetric + taskScoreVal + workoutScoreVal + studyScoreVal + routineScoreVal + waterScoreVal + goalScoreVal) / 7
        currentDisciplineScore.value = mainDisciplineScore
    }

    private fun isCompletedToday(habit: HabitEntity, dateStr: String): Boolean {
        return habit.completionHistory.split(",").contains(dateStr)
    }

    // --- Base Verification & Database Seeding Flow ---
    private fun checkAndSeedData() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync()
            if (user == null) {
                // Preload user registry profile
                repository.saveUser(
                    UserEntity(
                        id = "current_user",
                        name = "Alex Mercer",
                        age = 24,
                        gender = "Male",
                        occupation = "Software Engineer",
                        goals = "Nail early wake ups, exercise daily, log 4 liters of water, complete 100% tasks.",
                        targetWakeTime = "06:00 AM",
                        targetSleepTime = "10:30 PM",
                        weight = 73.5,
                        height = 180.0,
                        dailyTargetHours = 8.5,
                        profilePhotoPath = "avatar_3",
                        isRegistered = true,
                        currentStreak = 4,
                        longestStreak = 12
                    )
                )

                // Preload default daily tasks for today
                val defaultTasks = listOf(
                    TaskEntity(title = "Morning Stretch & Breathing", dueDate = currentDateStr, frequency = "Daily", category = "Routine", status = "Completed", priority = "Medium", timeOfDay = "Morning"),
                    TaskEntity(title = "Drink 500ml of hydration water", dueDate = currentDateStr, frequency = "Daily", category = "Routine", status = "Completed", priority = "Low", timeOfDay = "Morning"),
                    TaskEntity(title = "45-Min High-Intensity Workout", dueDate = currentDateStr, frequency = "Daily", category = "Workout", status = "Pending", priority = "High", timeOfDay = "Afternoon"),
                    TaskEntity(title = "Deep Work Block (Database Design)", dueDate = currentDateStr, frequency = "Daily", category = "Study", status = "InProgress", priority = "High", timeOfDay = "Afternoon"),
                    TaskEntity(title = "Read 15 Pages of Technical Journal", dueDate = currentDateStr, frequency = "Daily", category = "Study", status = "Pending", priority = "Medium", timeOfDay = "Evening"),
                    TaskEntity(title = "Unwind & Zero Screen Time routine", dueDate = currentDateStr, frequency = "Daily", category = "Routine", status = "Pending", priority = "Medium", timeOfDay = "Evening")
                )
                for (t in defaultTasks) repository.insertTask(t)

                // Preload habits
                val defaultHabits = listOf(
                    HabitEntity(name = "Wake Early (06:00 AM)", category = "Wake Early", streakCount = 4, maxStreak = 12, completionHistory = "$currentDateStr", reminderTime = "06:00 AM"),
                    HabitEntity(name = "Resistance Cardio Workout", category = "Exercise", streakCount = 2, maxStreak = 10, completionHistory = "", reminderTime = "05:00 PM"),
                    HabitEntity(name = "Read Books Regularly", category = "Read Books", streakCount = 5, maxStreak = 15, completionHistory = "$currentDateStr", reminderTime = "09:00 PM"),
                    HabitEntity(name = "Daily Guided Meditation", category = "Meditation", streakCount = 1, maxStreak = 8, completionHistory = "", reminderTime = "07:30 AM"),
                    HabitEntity(name = "Water Intake (2500ml target)", category = "Water Intake", streakCount = 3, maxStreak = 14, completionHistory = "$currentDateStr", reminderTime = "11:00 AM"),
                    HabitEntity(name = "Language & Code Practice", category = "Learning", streakCount = 3, maxStreak = 14, completionHistory = "$currentDateStr", reminderTime = "08:00 PM")
                )
                for (h in defaultHabits) repository.insertHabit(h)

                // Preload routines
                val defaultRoutines = listOf(
                    RoutineEntity(name = "Morning Routine", scheduleTime = "06:00 AM", tasksList = "Wake up cleanly, Rehydrate immediately, 10m Breathing flow, Outdoor light exposure", priority = "High"),
                    RoutineEntity(name = "Study Routine", scheduleTime = "09:00 AM", tasksList = "Set Pomodoro focus timer, Close social media tabs, Take notes, Active recall exercise", priority = "High"),
                    RoutineEntity(name = "Workout Routine", scheduleTime = "05:00 PM", tasksList = "Equilibrium warm up, Compound weight lifting, Cooldown mobility stretches", priority = "Medium"),
                    RoutineEntity(name = "Sleep Routine", scheduleTime = "10:00 PM", tasksList = "Set alarms for 6 AM, Charge phone across the room, Meditate or Read, Dim lights", priority = "High")
                )
                for (r in defaultRoutines) repository.insertRoutine(r)

                // Preload goals
                val defaultGoals = listOf(
                    GoalEntity(title = "Shred Fat & Build Muscle", description = "Drop weight to 70kg while maintaining high daily skeletal muscle composition", targetDate = "2026-09-30", type = "Medium-term", status = "InProgress", progress = 35),
                    GoalEntity(title = "Secure Cloud Certificated Engineer", description = "Pass the solutions architect professional exam before year end", targetDate = "2026-12-15", type = "Long-term", status = "InProgress", progress = 15),
                    GoalEntity(title = "Flawless Wake Up Streak", description = "Achieve 15 consecutive days waking up at exactly 6 AM", targetDate = "2026-07-05", type = "Short-term", status = "InProgress", progress = 40)
                )
                for (g in defaultGoals) repository.insertGoal(g)

                // Preload historical data (so graphs display beautifully out of the box)
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance()
                val scoreHistoryValues = listOf(
                    Pair(5, 74), Pair(4, 78), Pair(3, 80), Pair(2, 79), Pair(1, 84)
                )
                for ((daysAgo, score) in scoreHistoryValues) {
                    cal.time = Date()
                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    val histDate = formatter.format(cal.time)
                    repository.insertScore(
                        ScoreHistoryEntity(
                            date = histDate,
                            overallScore = score,
                            sleepScore = score + (1..6).random(),
                            workoutScore = score - (1..5).random(),
                            studyScore = score + (1..4).random(),
                            routineScore = score - (1..3).random()
                        )
                    )
                }

                // Initial notifications log
                repository.insertNotification(NotificationEntity(title = "Coach Sensei Active!", message = "Greetings, Alex. I am analyzing your daily goals and habit schedules. Click 'AI Coach' to begin our accountability logs.", type = "Tip"))
                repository.insertNotification(NotificationEntity(title = "Goal Updated", message = "Target alarm set dynamically for 06:00 AM.", type = "Alert"))

                // Preload achievements/badges template
                val badges = listOf(
                    AchievementEntity(title = "Early Bird", description = "Woke up at target time 3 days in a row.", iconName = "badge_early", isUnlocked = true, unlockedAt = System.currentTimeMillis()),
                    AchievementEntity(title = "Productivity Master", description = "Completed 15 core focus tasks under timer.", iconName = "badge_master", isUnlocked = false),
                    AchievementEntity(title = "Consistency Champion", description = "Logged standard habit targets consecutively for 7 days.", iconName = "badge_champion", isUnlocked = true, unlockedAt = System.currentTimeMillis()),
                    AchievementEntity(title = "7 Day Discipline", description = "Hit overall daily discipline average score >80% for 7 days.", iconName = "badge_7", isUnlocked = false),
                    AchievementEntity(title = "30 Day Discipline", description = "Build flawless self-control and log routines consecutively.", iconName = "badge_30", isUnlocked = false),
                    AchievementEntity(title = "Hydration Hero", description = "Hit 2000ml water target for 5 consecutive days", iconName = "badge_water", isUnlocked = true, unlockedAt = System.currentTimeMillis())
                )
                for (b in badges) repository.insertAchievement(b)

                // Initial greeting messages in chat
                repository.insertChatMessage(ChatMessageEntity(sender = "AI", message = "Greetings, Alex Mercer. I am 'Sensei AI', your elite Discipline and Accountability Coach. I have analyzed your profiles and goals.\n\nYou have completed 'Morning Stretch & Breathing' and 'Drink 500ml water' targets today. Your current estimated Daily Discipline score is 85%.\n\nTell me: did you execute your deep work and workout routines yet? Let's keep driving forward!"))
            }
        }
    }

    // --- User Actions ---
    fun updateProfile(name: String, age: Int, gender: String, occupation: String, goals: String, wakeTime: String, sleepTime: String, weight: Double, height: Double, dailyHours: Double) {
        viewModelScope.launch {
            val updatedUser = UserEntity(
                id = "current_user",
                name = name,
                age = age,
                gender = gender,
                occupation = occupation,
                goals = goals,
                targetWakeTime = wakeTime,
                targetSleepTime = sleepTime,
                weight = weight,
                height = height,
                dailyTargetHours = dailyHours,
                isRegistered = true
            )
            repository.saveUser(updatedUser)
            
            // Log local AI confirmation notification
            repository.insertNotification(NotificationEntity(
                title = "Profile recalibrated!",
                message = "Sensei AI updated your routines and target tracking rules based on your new discipline goals.",
                type = "Alert"
            ))

            // Inform coach of profile adjustment
            repository.insertChatMessage(ChatMessageEntity(sender = "User", message = "I updated my profile. Targets changed!"))
            val response = repository.askGeminiCoach(chatMessages.value, "I updated my profile targets.", updatedUser)
            repository.insertChatMessage(ChatMessageEntity(sender = "AI", message = response))
        }
    }

    fun submitChatMessage(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            // Write User message to DB
            repository.insertChatMessage(ChatMessageEntity(sender = "User", message = message))
            aiIsTyping.value = true

            try {
                // Post-process message text for tracker additions or triggers
                val lowercaseMsg = message.lowercase()
                when {
                    lowercaseMsg.contains("water") || lowercaseMsg.contains("drank") -> {
                        // Dynamically update water log
                        val currentAmt = _currentWaterLog.value?.amountMl ?: 0
                        logWater(currentAmt + 250)
                    }
                    lowercaseMsg.contains("workout") || lowercaseMsg.contains("exercise") -> {
                        // Toggle a pending workout task to completed
                        tasks.value.filter { it.category == "Workout" && it.status != "Completed" }.firstOrNull()?.let {
                            updateTaskStatus(it, "Completed")
                        }
                    }
                }

                // Call Gemini coach
                val result = repository.askGeminiCoach(chatMessages.value, message, userProfile.value)
                repository.insertChatMessage(ChatMessageEntity(sender = "AI", message = result))
                
                // Fire smart notification
                repository.insertNotification(NotificationEntity(
                    title = "Sensei Coaching Tip",
                    message = "Check AI Coach for actionable advice on your daily structure.",
                    type = "Tip"
                ))
            } catch (e: Exception) {
                repository.insertChatMessage(ChatMessageEntity(sender = "AI", message = "Forgive me, my neural processors are recalibrating. Let's redirect our focus on maintaining our workout and wake streaks!"))
            } finally {
                aiIsTyping.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            repository.insertChatMessage(ChatMessageEntity(sender = "AI", message = "Chat memory deleted successfully. Let's draft a new chapter of discipline!"))
        }
    }

    // --- Task CRUD Processes ---
    fun addTask(title: String, category: String, priority: String, timeOfDay: String, frequency: String = "Daily") {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                dueDate = currentDateStr,
                frequency = frequency,
                category = category,
                status = "Pending",
                priority = priority,
                timeOfDay = timeOfDay,
                isCustom = true
            )
            repository.insertTask(task)
            recalculateDisciplineScores()
            repository.insertNotification(NotificationEntity(title = "New Task Added", message = "'$title' was populated to target timeline.", type = "Tip"))
        }
    }

    fun updateTaskStatus(task: TaskEntity, nextStatus: String) {
        viewModelScope.launch {
            val updated = task.copy(status = nextStatus)
            repository.updateTask(updated)
            recalculateDisciplineScores()
            
            if (nextStatus == "Completed") {
                repository.insertNotification(NotificationEntity(
                    title = "Habit Accomplished",
                    message = "Boom! Completed: '${task.title}' +15 Discipline points.",
                    type = "Alert"
                ))
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            recalculateDisciplineScores()
        }
    }

    // --- Habits Tracking ---
    fun toggleHabitCompletion(habit: HabitEntity) {
        viewModelScope.launch {
            val history = habit.completionHistory.split(",").toMutableList().filter { it.isNotBlank() }
            val completedToday = history.contains(currentDateStr)
            
            val updatedHistory = if (completedToday) {
                history.filter { it != currentDateStr }.joinToString(",")
            } else {
                (history + currentDateStr).joinToString(",")
            }

            val nextStreak = if (completedToday) {
                kotlin.math.max(0, habit.streakCount - 1)
            } else {
                habit.streakCount + 1
            }

            val nextMaxStreak = kotlin.math.max(habit.maxStreak, nextStreak)

            val updatedHabit = habit.copy(
                completionHistory = updatedHistory,
                streakCount = nextStreak,
                maxStreak = nextMaxStreak
            )
            repository.updateHabit(updatedHabit)
            recalculateDisciplineScores()
            
            if (!completedToday) {
                repository.insertNotification(NotificationEntity(
                    title = "Habit Logged", 
                    message = "Logged: ${habit.name}. Stay consistent!", 
                    type = "Alert"
                ))
            }
        }
    }

    // --- Water tracker ---
    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            val currentLog = _currentWaterLog.value ?: WaterLogEntity(date = currentDateStr, amountMl = 0)
            val updatedLog = currentLog.copy(amountMl = amountMl)
            repository.saveWaterLog(updatedLog)
            recalculateDisciplineScores()
        }
    }

    // --- Sleep tracker ---
    fun logSleepAndWake(wakeTime: String, sleepTime: String, wakeUpScore: Int, sleepScore: Int) {
        viewModelScope.launch {
            val log = SleepLogEntity(
                date = currentDateStr,
                actualWakeTime = wakeTime,
                actualSleepTime = sleepTime,
                wakeUpConsistencyScore = wakeUpScore,
                sleepConsistencyScore = sleepScore
            )
            repository.saveSleepLog(log)
            recalculateDisciplineScores()
            repository.insertNotification(NotificationEntity(
                title = "Sleep Stats Logged",
                message = "Wake consistency evaluated at: $wakeUpScore% score. Foundation secured.",
                type = "Alert"
            ))
        }
    }

    // --- Goal CRUD Processes ---
    fun addGoal(title: String, description: String, targetDate: String, type: String) {
        viewModelScope.launch {
            val g = GoalEntity(
                title = title,
                description = description,
                targetDate = targetDate,
                type = type,
                status = "InProgress",
                progress = 0
            )
            repository.insertGoal(g)
            recalculateDisciplineScores()
        }
    }

    fun updateGoalProgress(goal: GoalEntity, progress: Int) {
        viewModelScope.launch {
            val status = if (progress >= 100) "Completed" else "InProgress"
            val updated = goal.copy(progress = progress, status = status)
            repository.updateGoal(updated)
            recalculateDisciplineScores()
            
            if (progress >= 100) {
                repository.insertNotification(NotificationEntity(
                    title = "Milestone Unlocked!",
                    message = "Victory! Goal reached: '${goal.title}'. AI calibrated.",
                    type = "Alert"
                ))
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
            recalculateDisciplineScores()
        }
    }

    // --- Pomodoro Focusing System ---
    private fun startPomodoroClock() {
        viewModelScope.launch {
            while (true) {
                if (focusTimerIsRunning.value && focusTimeLeft.value > 0) {
                    delay(1000)
                    focusTimeLeft.value -= 1
                    if (focusTimeLeft.value == 0) {
                        onFocusTimerFinished()
                    }
                } else {
                    delay(500)
                }
            }
        }
    }

    private fun onFocusTimerFinished() {
        focusTimerIsRunning.value = false
        if (focusTimerPhase.value == "Focus") {
            // Completed 1 Focus session! Set to study completion high, credit user.
            viewModelScope.launch {
                repository.insertNotification(NotificationEntity(
                    title = "Pomodoro Finished!",
                    message = "Exceptional Focus! You logged 25 minutes of high intellectual focus.",
                    type = "Alert"
                ))
                // Create custom learning task or set existing Study tasks to Complete
                val todayTasks = tasks.value.filter { it.dueDate == currentDateStr && it.category == "Study" }
                if (todayTasks.isNotEmpty()) {
                    updateTaskStatus(todayTasks.first(), "Completed")
                } else {
                    addTask("Study session completed under Pomodoro", "Study", "Medium", "Afternoon")
                }
            }
            focusTimerPhase.value = "Short Break"
            focusTimeLeft.value = 300 // 5 minutes
            focusTimerTotalMinutes.value = 5
        } else {
            // Break is over! Reset to focus
            focusTimerPhase.value = "Focus"
            focusTimeLeft.value = 1500
            focusTimerTotalMinutes.value = 25
        }
    }

    fun toggleFocusTimer() {
        focusTimerIsRunning.value = !focusTimerIsRunning.value
    }

    fun resetFocusTimer() {
        focusTimerIsRunning.value = false
        if (focusTimerPhase.value == "Focus") {
            focusTimeLeft.value = 1500
        } else {
            focusTimeLeft.value = 300
        }
    }

    fun updateRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.updateRoutine(routine)
        }
    }
}
