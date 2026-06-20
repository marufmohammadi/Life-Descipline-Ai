package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// Theme Colors
val ObsidianBackground = Color(0xFF0F1115)
val DeepSlateSurface = Color(0xFF1C1F26)
val IndigoNeon = Color(0xFF6366F1)
val AccentTeal = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val RoseCrimson = Color(0xFFEF4444)
val SlateTextPrimary = Color(0xFFF1F5F9)
val SlateTextSecondary = Color(0xFF94A3B8)
val SlateBorder = Color(0xFF1E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val context = LocalContext.current
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val notificationList by viewModel.notifications.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showProfileSetupDialog by remember { mutableStateOf(false) }
    var activeTab by viewModel.currentTab

    // Safe layout configuration matching Android styling rules
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(listOf(IndigoNeon, AccentTeal)),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OfflineBolt,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Life Discipline AI",
                            color = SlateTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
                actions = {
                    // Profile setup trigger & Smart alert counter
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(DeepSlateSurface, CircleShape)
                            .clickable { showProfileSetupDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile Setup",
                            tint = IndigoNeon,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    val unreadCount = notificationList.size
                    if (unreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = RoseCrimson) {
                                    Text("$unreadCount", color = Color.White, fontSize = 9.sp)
                                }
                            },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Alerts",
                                tint = SlateTextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBackground,
                    titleContentColor = SlateTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepSlateSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val items = listOf(
                    Triple("Dashboard", Icons.Filled.SpaceDashboard, Icons.Outlined.SpaceDashboard),
                    Triple("Coaching", Icons.Filled.Forum, Icons.Outlined.Forum),
                    Triple("Schedules", Icons.Filled.ListAlt, Icons.Outlined.ListAlt),
                    Triple("Focus Tracker", Icons.Filled.Timer, Icons.Outlined.Timer),
                    Triple("Statistics", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
                    Triple("System Control", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
                )

                items.forEach { (tabName, filledIcon, outlinedIcon) ->
                    NavigationBarItem(
                        selected = activeTab == tabName,
                        onClick = { activeTab = tabName },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == tabName) filledIcon else outlinedIcon,
                                contentDescription = tabName,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tabName,
                                fontSize = 10.sp,
                                fontWeight = if (activeTab == tabName) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = SlateTextSecondary,
                            selectedTextColor = IndigoNeon,
                            unselectedTextColor = SlateTextSecondary,
                            indicatorColor = IndigoNeon
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == "Schedules") {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = IndigoNeon,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_task_fab")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianBackground)
        ) {
            Crossfade(targetState = activeTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    "Dashboard" -> DashboardScreen(viewModel, onProfileClick = { showProfileSetupDialog = true })
                    "Coaching" -> CoachingChatScreen(viewModel)
                    "Schedules" -> RoutineAndTaskScreen(viewModel)
                    "Focus Tracker" -> FocusAndWaterScreen(viewModel)
                    "Statistics" -> StatisticsAndBadgesScreen(viewModel)
                    "System Control" -> AdminScreen(viewModel)
                }
            }
        }
    }

    // Modal dialog overlays
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, cat, prio, timeValue ->
                viewModel.addTask(title, cat, prio, timeValue)
                showAddTaskDialog = false
            }
        )
    }

    if (showProfileSetupDialog) {
        ProfileSetupDialog(
            user = profileState,
            onDismiss = { showProfileSetupDialog = false },
            onSave = { name, age, gender, occ, goals, wake, sleep, wVal, hVal, hrs ->
                viewModel.updateProfile(name, age, gender, occ, goals, wake, sleep, wVal, hVal, hrs)
                showProfileSetupDialog = false
            }
        )
    }
}

// ==========================================
// 1. DASHBOARD COMPOSABLE
// ==========================================
@Composable
fun DashboardScreen(viewModel: MainViewModel, onProfileClick: () -> Unit) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val waterLog by viewModel.currentWaterLog.collectAsStateWithLifecycle()
    val moodValue by viewModel.currentMood

    val overallScore by viewModel.currentDisciplineScore
    val sleepScore by viewModel.currentSleepScore
    val workoutScore by viewModel.currentWorkoutScore
    val studyScore by viewModel.currentStudyScore
    val routineScore by viewModel.currentRoutineScore

    val scrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Quote Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() },
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HELLO, CHAMPION",
                            color = IndigoNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profileState?.name ?: "Alex Mercer",
                            color = SlateTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"A disciplined mind leads to happy outcomes. Conquering self is the ultimate victory.\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextSecondary,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Streak badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(ObsidianBackground, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🔥 ${profileState?.currentStreak ?: 4}D",
                            color = WarningAmber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "STREAK",
                            color = SlateTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Animated Master Discipline Score Radar / Speedometer Arc
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DYNAMIC DISCIPLINE EVALUATION",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Custom Canvas Dial
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Empty track
                            drawArc(
                                color = ObsidianBackground,
                                startAngle = -220f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                            // Progress track
                            val fillSweep = (overallScore.toFloat() / 100f) * 260f
                            drawArc(
                                brush = Brush.sweepGradient(listOf(IndigoNeon, AccentTeal, IndigoNeon)),
                                startAngle = -220f,
                                sweepAngle = fillSweep,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$overallScore%",
                                color = SlateTextPrimary,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "SENSEI RANK",
                                color = AccentTeal,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Grid Metrics Checklist
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "LATEST VITAL STATISTICS",
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Sleep & Alarm",
                        metric = "$sleepScore%",
                        sub = profileState?.targetWakeTime ?: "06:00 AM",
                        color = AccentTeal,
                        icon = Icons.Filled.Bedtime,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Workout Force",
                        metric = "$workoutScore%",
                        sub = "45 mins active",
                        color = IndigoNeon,
                        icon = Icons.Filled.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Focus & Core",
                        metric = "$studyScore%",
                        sub = "Pomodoro logging",
                        color = WarningAmber,
                        icon = Icons.Filled.Lightbulb,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Routines Done",
                        metric = "$routineScore%",
                        sub = "Checklists on track",
                        color = RoseCrimson,
                        icon = Icons.Filled.OfflinePin,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Water Counter Mini-Widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalMall,
                        contentDescription = "Water tracking",
                        tint = IndigoNeon,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HYDRATION CHECKPOINT",
                            color = SlateTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${waterLog?.amountMl ?: 0} / ${waterLog?.targetMl ?: 2000} ml",
                            color = SlateTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = {
                                val log = waterLog
                                if (log != null && log.targetMl > 0) {
                                    (log.amountMl.toFloat() / log.targetMl.toFloat()).coerceIn(0f, 1f)
                                } else 0f
                            },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = IndigoNeon,
                            trackColor = ObsidianBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.logWater((waterLog?.amountMl ?: 0) + 250) },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+250ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Habit Quick completion checklist for the day
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "QUICK HABIT TRACKER CONSOLE",
                        color = SlateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val activeHabits by viewModel.habits.collectAsStateWithLifecycle()
                    if (activeHabits.isEmpty()) {
                        Text("No active habits registered.", color = SlateTextSecondary, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeHabits.take(3).forEach { habit ->
                                val completedToday = habit.completionHistory.split(",").contains(viewModel.currentDateStr)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ObsidianBackground, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleHabitCompletion(habit) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (completedToday) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = "Toggle",
                                        tint = if (completedToday) AccentTeal else SlateTextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = habit.name,
                                        color = if (completedToday) SlateTextSecondary else SlateTextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "🔥 ${habit.streakCount}D",
                                        color = WarningAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mood tracking system
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCOUNTABILITY MOOD METRIC",
                        color = SlateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val moods = listOf("Focused", "Energized", "Calmed", "Tired", "Anxious")
                        moods.forEach { m ->
                            val isSelected = moodValue == m
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) IndigoNeon else ObsidianBackground,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.currentMood.value = m }
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m,
                                    color = if (isSelected) Color.White else SlateTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    metric: String,
    sub: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = SlateTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = metric,
                color = SlateTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sub,
                color = SlateTextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// 2. COACHING CHAT SCREEN (GEMINI LOGIC)
// ==========================================
@Composable
fun CoachingChatScreen(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.aiIsTyping
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Coach Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(IndigoNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MilitaryTech,
                        contentDescription = "Sensei Master",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sensei Accountability Engine",
                        color = SlateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Continuous active cognitive tracking",
                        color = AccentTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(
                    onClick = { viewModel.clearChat() },
                    colors = ButtonDefaults.textButtonColors(contentColor = RoseCrimson)
                ) {
                    Text("Reset Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chats lists
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "User"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.82f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) IndigoNeon else DeepSlateSurface
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isUser) 12.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 12.dp
                        ),
                        border = if (isUser) null else BorderStroke(1.dp, SlateBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "ALEX" else "SENSEI AI",
                                color = if (isUser) Color.White.copy(0.7f) else AccentTeal,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.message,
                                color = Color.White,
                                fontSize = 14.sp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            if (isTyping) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SlateBorder)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = IndigoNeon,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sensei is calculating your habits...",
                                    color = SlateTextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dialogue Suggestions chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            val suggestions = listOf(
                "Rate my wake up today",
                "Log water 500ml",
                "How do I boost focus?",
                "Suggest study routine"
            )
            items(suggestions) { text ->
                Box(
                    modifier = Modifier
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                        .background(DeepSlateSurface, RoundedCornerShape(16.dp))
                        .clickable { viewModel.submitChatMessage(text) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = text, color = SlateTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Text input field bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Report morning routine, wake times, workouts...", color = SlateTextSecondary) },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                    .testTag("chat_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DeepSlateSurface,
                    unfocusedContainerColor = DeepSlateSurface,
                    disabledContainerColor = DeepSlateSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 2
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.submitChatMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .background(IndigoNeon, CircleShape)
                    .size(48.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

// ==========================================
// 3. ROUTINES & TASK SCREEN
// ==========================================
@Composable
fun RoutineAndTaskScreen(viewModel: MainViewModel) {
    val taskList by viewModel.tasks.collectAsStateWithLifecycle()
    val rawRoutines by viewModel.routines.collectAsStateWithLifecycle()
    var displaySection by remember { mutableStateOf("Tasks") } // Tasks, Routines

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Selector Header Tap Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                .background(DeepSlateSurface, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (displaySection == "Tasks") IndigoNeon else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { displaySection = "Tasks" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Daily Checklist",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (displaySection == "Routines") IndigoNeon else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { displaySection = "Routines" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Core Routines",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (displaySection == "Tasks") {
            // Checklist filter for today
            val todayTasks = taskList.filter { it.dueDate == viewModel.currentDateStr }
            
            if (todayTasks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ListAlt, contentDescription = null, tint = SlateTextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Task database completed or empty today!", color = SlateTextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Add a custom task using the green + button below.", color = SlateTextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todayTasks) { task ->
                        val isDone = task.status == "Completed"
                        val containerCardBkg = if (isDone) DeepSlateSurface.copy(0.4f) else DeepSlateSurface

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val next = if (isDone) "Pending" else "Completed"
                                    viewModel.updateTaskStatus(task, next)
                                },
                            colors = CardDefaults.cardColors(containerColor = containerCardBkg),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SlateBorder.copy(alpha = if (isDone) 0.5f else 1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = "Toggle status",
                                    tint = if (isDone) AccentTeal else SlateTextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        color = if (isDone) SlateTextSecondary else SlateTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .background(ObsidianBackground, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(task.category, color = IndigoNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(ObsidianBackground, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(task.priority, color = if (task.priority == "High") RoseCrimson else WarningAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Remove", tint = SlateTextSecondary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Routines core structures
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rawRoutines) { r ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SlateBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.OfflineBolt,
                                        contentDescription = r.name,
                                        tint = AccentTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(r.name, color = SlateTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Switch(
                                    checked = r.isActive,
                                    onCheckedChange = { viewModel.updateRoutine(r.copy(isActive = it)) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IndigoNeon)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "SCHEDULE TARGET: " + r.scheduleTime + " • PRIORITY: " + r.priority,
                                color = SlateTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = ObsidianBackground, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val listItems = r.tasksList.split(",")
                            listItems.forEach { subItem ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = IndigoNeon,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(subItem.trim(), color = SlateTextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. FOCUS & HYDRATION SCREEN
// ==========================================
@Composable
fun FocusAndWaterScreen(viewModel: MainViewModel) {
    val timeLeft by viewModel.focusTimeLeft
    val isTimerActive by viewModel.focusTimerIsRunning
    val currentPhase by viewModel.focusTimerPhase
    val totalMins by viewModel.focusTimerTotalMinutes

    val waterLog by viewModel.currentWaterLog.collectAsStateWithLifecycle()
    val sleepLog by viewModel.currentSleepLog.collectAsStateWithLifecycle()

    var showSleepLogDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pomodoro Clock Segment
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FOCUS WORK ENGINE (POMODORO)",
                        color = SlateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer Dial
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Empty track
                            drawCircle(
                                color = ObsidianBackground,
                                radius = size.minDimension / 2 - 10.dp.toPx(),
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Progress bar circular ring
                            val maxSecs = totalMins * 60
                            val pct = if (maxSecs > 0) timeLeft.toFloat() / maxSecs.toFloat() else 1f
                            drawArc(
                                color = if (currentPhase == "Focus") IndigoNeon else AccentTeal,
                                startAngle = -90f,
                                sweepAngle = pct * 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }

                        // Countdown numbers text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val mins = timeLeft / 60
                            val secs = timeLeft % 60
                            val displayTime = String.format("%02d:%02d", mins, secs)
                            Text(
                                text = displayTime,
                                color = SlateTextPrimary,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = currentPhase.uppercase(),
                                color = if (currentPhase == "Focus") WarningAmber else AccentTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.toggleFocusTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerActive) RoseCrimson else IndigoNeon
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isTimerActive) "Pause Session" else "Start Session", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.resetFocusTimer() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }

        // Hydration tracker dashboard slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANUAL DEEP WATER TRACKING",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentMl = waterLog?.amountMl ?: 0
                    val targetMl = waterLog?.targetMl ?: 2000

                    Text(
                        text = "Today logged: $currentMl ml of $targetMl ml target",
                        color = SlateTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated sliding track
                    Slider(
                        value = currentMl.toFloat(),
                        onValueChange = { viewModel.logWater(it.toInt()) },
                        valueRange = 0f..4000f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = IndigoNeon,
                            activeTrackColor = IndigoNeon,
                            inactiveTrackColor = ObsidianBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.logWater(currentMl + 250) },
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+250 ml", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.logWater(currentMl + 500) },
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+500 ml", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.logWater(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseCrimson.copy(0.2f)),
                            modifier = Modifier.weight(0.6f)
                        ) {
                            Text("Zero", fontSize = 11.sp, color = RoseCrimson)
                        }
                    }
                }
            }
        }

        // Sleep logs checker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTUAL SLEEP LOG",
                            color = SlateTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Button(
                            onClick = { showSleepLogDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Log Sleep", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Today wake-up score: " + (sleepLog?.wakeUpConsistencyScore ?: 100) + "% consistency score",
                        color = SlateTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Woke up at: " + (sleepLog?.actualWakeTime ?: "06:00 AM") + " • Target: 06:00 AM",
                        color = SlateTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (showSleepLogDialog) {
        SleepLogDialog(
            onDismiss = { showSleepLogDialog = false },
            onSave = { wake, sleep, wakeScr, sleepScr ->
                viewModel.logSleepAndWake(wake, sleep, wakeScr, sleepScr)
                showSleepLogDialog = false
            }
        )
    }
}

// ==========================================
// 5. STATISTICS & GOAL & BADGES COMPOSABLE (PDF)
// ==========================================
@Composable
fun StatisticsAndBadgesScreen(viewModel: MainViewModel) {
    val scoresList by viewModel.scoreHistory.collectAsStateWithLifecycle()
    val badgeList by viewModel.achievements.collectAsStateWithLifecycle()
    val rawGoals by viewModel.goals.collectAsStateWithLifecycle()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var reportExportedNotice by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // glowing Canvas custom line chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HISTORICAL DISCIPLINE SCORES",
                        color = SlateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (scoresList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp), contentAlignment = Alignment.Center
                        ) {
                            Text("Calculating history scores...", color = SlateTextSecondary)
                        }
                    } else {
                        // Custom Drawn Canvas Line Chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            val spacing = size.width / (scoresList.size + 1)
                            val yMax = 100f
                            val strokeWidthVal = 4f

                            val points = scoresList.mapIndexed { i, scoreEntity ->
                                val x = spacing * (i + 1)
                                val pct = scoreEntity.overallScore.toFloat() / yMax
                                val y = size.height - (size.height * pct)
                                Offset(x, y)
                            }

                            // Draw gradient area first
                            val gradientPath = Path().apply {
                                if (points.isNotEmpty()) {
                                    moveTo(points.first().x, size.height)
                                    points.forEach { lineTo(it.x, it.y) }
                                    lineTo(points.last().x, size.height)
                                    close()
                                }
                            }
                            drawPath(
                                path = gradientPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(IndigoNeon.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )

                            // Draw Line
                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = IndigoNeon,
                                    start = points[i],
                                    end = points[i + 1],
                                    strokeWidth = strokeWidthVal
                                )
                            }

                            // Draw Hover Circles and point values
                            points.forEachIndexed { idx, point ->
                                drawCircle(
                                    color = AccentTeal,
                                    radius = 12f,
                                    center = point
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 6f,
                                    center = point
                                )
                            }
                        }
                        
                        // Chart horizontal timeline tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            scoresList.forEach { s ->
                                Text(
                                    text = s.date.takeLast(5),
                                    color = SlateTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export Report Section (Share dialog)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DISCIPLINE REPORT HUB",
                            color = SlateTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Export accountability summary data",
                            color = SlateTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = {
                            reportExportedNotice = true
                            // Trigger real Android share intent detailing accountability
                            val shareBody = """
                                === LIFE DISCIPLINE AI: ACCOUNTABILITY SUMMARY ===
                                Generated Date: 2026-06-20
                                Current Streak: 4 Days
                                Dynamic Discipline Score: 85%
                                Sleep Consistency Rank: Exceptional (90% rating)
                                Tasks Status: 100% daily core checklist loaded.
                            """.trimIndent()
                            
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Discipline Report"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Share CSV", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Goal lists management
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DISCIPLINE GOAL MAP", color = SlateTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showAddGoalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("+ Goal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (rawGoals.isEmpty()) {
            item {
                Text("No active goals registered.", color = SlateTextSecondary, fontSize = 12.sp)
            }
        } else {
            items(rawGoals) { goal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(goal.title, color = SlateTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.deleteGoal(goal.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Delete", tint = RoseCrimson)
                            }
                        }
                        Text(goal.description, color = SlateTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Date: " + goal.targetDate, color = WarningAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${goal.progress}% completed", color = AccentTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Sliding adjustments
                        Slider(
                            value = goal.progress.toFloat(),
                            onValueChange = { viewModel.updateGoalProgress(goal, it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = IndigoNeon, activeTrackColor = IndigoNeon)
                        )
                    }
                }
            }
        }

        // Badges Achievements unlocked
        item {
            Text("UNLOCKED HONOR SHIELDS", color = SlateTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(badgeList) { badge ->
                    val isLocked = !badge.isUnlocked
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .background(Color.Transparent),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLocked) DeepSlateSurface.copy(alpha = 0.4f) else DeepSlateSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SlateBorder.copy(alpha = if (isLocked) 0.4f else 1f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(if (isLocked) ObsidianBackground else IndigoNeon, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.Stars,
                                    contentDescription = badge.title,
                                    tint = if (isLocked) SlateTextSecondary else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                badge.title,
                                color = if (isLocked) SlateTextSecondary else SlateTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                badge.description,
                                color = SlateTextSecondary,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAdd = { title, desc, date ->
                viewModel.addGoal(title, desc, date, "Short-term")
                showAddGoalDialog = false
            }
        )
    }
}

// ==========================================
// 6. ADMIN / SYSTEM CONTROL COMPOSABLE
// ==========================================
@Composable
fun AdminScreen(viewModel: MainViewModel) {
    val tasksSize by viewModel.tasks.collectAsStateWithLifecycle()
    val habitsSize by viewModel.habits.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatMessages.collectAsStateWithLifecycle()
    val alertsList by viewModel.notifications.collectAsStateWithLifecycle()
    val userProfileSetting by viewModel.userProfile.collectAsStateWithLifecycle()

    var customNotificationMsg by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM ENGINE DIAGNOSTICS & TELEMETRY",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Database Tasks: " + tasksSize.size, color = SlateTextPrimary, fontSize = 13.sp)
                            Text("Database Habits: " + habitsSize.size, color = SlateTextPrimary, fontSize = 13.sp)
                        }
                        Column {
                            Text("Chat Memory Size: " + chatHistory.size, color = SlateTextPrimary, fontSize = 13.sp)
                            Text("Diagnostic Alerts: " + alertsList.size, color = SlateTextPrimary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ADMIN INJECTOR ACTIONS",
                        color = WarningAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.addTask("Generated Daily Cardio run", "Workout", "Medium", "Afternoon")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mock Task", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.clearChat() },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseCrimson.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Wipe Memory", color = RoseCrimson, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BROADCAST CUSTOM ALERTS",
                        color = SlateTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = customNotificationMsg,
                        onValueChange = { customNotificationMsg = it },
                        placeholder = { Text("Enter prompt message to broadcast locally...", color = SlateTextSecondary) },
                        modifier = Modifier.fillMaxWidth().border(1.dp, SlateBorder, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ObsidianBackground,
                            unfocusedContainerColor = ObsidianBackground,
                            focusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (customNotificationMsg.isNotBlank()) {
                                viewModel.submitChatMessage(customNotificationMsg)
                                customNotificationMsg = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Broadcast Custom Log Alert", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// DIALOG OVERLAY CREATIONS
// ==========================================

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Workout") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedTimeOfDay by remember { mutableStateOf("Afternoon") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Draft New Target Day Task", color = SlateTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_input")
                )

                Text("Category", fontSize = 12.sp, color = SlateTextSecondary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Workout", "Study", "Routine").forEach { cat ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (selectedCategory == cat) Color.Transparent else SlateBorder, RoundedCornerShape(8.dp))
                                .background(if (selectedCategory == cat) IndigoNeon else DeepSlateSurface, RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(cat, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Text("Priority Level", fontSize = 12.sp, color = SlateTextSecondary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("High", "Medium", "Low").forEach { pri ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (selectedPriority == pri) Color.Transparent else SlateBorder, RoundedCornerShape(8.dp))
                                .background(if (selectedPriority == pri) IndigoNeon else DeepSlateSurface, RoundedCornerShape(8.dp))
                                .clickable { selectedPriority = pri }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(pri, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title, selectedCategory, selectedPriority, selectedTimeOfDay) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon),
                modifier = Modifier.testTag("add_task_dialog_confirm")
            ) {
                Text("Confirm Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoseCrimson)
            }
        },
        containerColor = DeepSlateSurface
    )
}

@Composable
fun ProfileSetupDialog(
    user: UserEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Int, String, String, String, String, String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "Alex Mercer") }
    var ageStr by remember { mutableStateOf((user?.age ?: 24).toString()) }
    var occupation by remember { mutableStateOf(user?.occupation ?: "Software Engineer") }
    var goals by remember { mutableStateOf(user?.goals ?: "") }
    var wakeTime by remember { mutableStateOf(user?.targetWakeTime ?: "06:00 AM") }
    var sleepTime by remember { mutableStateOf(user?.targetSleepTime ?: "10:30 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Secure User Settings Console", color = SlateTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                TextField(value = ageStr, onValueChange = { ageStr = it }, label = { Text("Age (Years)") }, modifier = Modifier.fillMaxWidth())
                TextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Occupation") }, modifier = Modifier.fillMaxWidth())
                TextField(value = goals, onValueChange = { goals = it }, label = { Text("Target Discipline Goals") }, modifier = Modifier.fillMaxWidth())
                TextField(value = wakeTime, onValueChange = { wakeTime = it }, label = { Text("Alarm Target Wake Time") }, modifier = Modifier.fillMaxWidth())
                TextField(value = sleepTime, onValueChange = { sleepTime = it }, label = { Text("Alarm Target Sleep Time") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ageInt = ageStr.toIntOrNull() ?: 24
                    onSave(name, ageInt, "Male", occupation, goals, wakeTime, sleepTime, 73.5, 180.0, 8.5)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon)
            ) {
                Text("Calibrate Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoseCrimson)
            }
        },
        containerColor = DeepSlateSurface
    )
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Establish Clear Discipline Goal", color = SlateTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Goal Milestone Title") }, modifier = Modifier.fillMaxWidth())
                TextField(value = desc, onValueChange = { desc = it }, label = { Text("Description & Requirements") }, modifier = Modifier.fillMaxWidth())
                TextField(value = date, onValueChange = { date = it }, label = { Text("Target Deadline Date") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title, desc, date) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon)
            ) {
                Text("Establish Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoseCrimson)
            }
        },
        containerColor = DeepSlateSurface
    )
}

@Composable
fun SleepLogDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int) -> Unit
) {
    var actualWake by remember { mutableStateOf("06:15 AM") }
    var actualSleep by remember { mutableStateOf("10:45 PM") }
    var wakeScoreStr by remember { mutableStateOf("90") }
    var sleepScoreStr by remember { mutableStateOf("85") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Daily Arc Sleep Stats", color = SlateTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(value = actualWake, onValueChange = { actualWake = it }, label = { Text("Actual Wake Time") }, modifier = Modifier.fillMaxWidth())
                TextField(value = actualSleep, onValueChange = { actualSleep = it }, label = { Text("Actual Sleep Time") }, modifier = Modifier.fillMaxWidth())
                TextField(value = wakeScoreStr, onValueChange = { wakeScoreStr = it }, label = { Text("Wake Consistency Score (0-100)") }, modifier = Modifier.fillMaxWidth())
                TextField(value = sleepScoreStr, onValueChange = { sleepScoreStr = it }, label = { Text("Sleep Consistency Score (0-100)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wS = wakeScoreStr.toIntOrNull() ?: 90
                    val sS = sleepScoreStr.toIntOrNull() ?: 85
                    onSave(actualWake, actualSleep, wS, sS)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoNeon)
            ) {
                Text("Log Stats")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoseCrimson)
            }
        },
        containerColor = DeepSlateSurface
    )
}
