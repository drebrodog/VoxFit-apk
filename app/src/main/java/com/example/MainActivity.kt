package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.NavItem
import com.example.ui.components.AddExerciseDialog
import com.example.ui.components.ClearDataDialog
import com.example.ui.components.EditSetDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.FinishWorkoutDialog
import com.example.ui.components.VoiceHelpDialog
import com.example.ui.components.VoxBottomNav
import com.example.ui.components.VoxFitTopBar
import com.example.ui.components.WorkoutDetailsBottomSheet
import com.example.ui.screens.ExercisesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.VoxBackground
import com.example.ui.theme.VoxFitTheme
import com.example.viewmodel.VoxFitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoxFitTheme {
                VoxFitApp()
            }
        }
    }
}

@Composable
fun VoxFitApp(
    viewModel: VoxFitViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(uiState.needRecordAudioPermission) {
        if (uiState.needRecordAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(VoxBackground)
            .testTag("voxfit_main_scaffold"),
        containerColor = VoxBackground,
        topBar = {
            VoxFitTopBar(
                timerText = uiState.formattedTimer,
                isTimerRunning = uiState.isTimerRunning,
                onToggleTimer = { viewModel.toggleTimer() },
                onFinishWorkout = { viewModel.showFinishWorkoutDialog() }
            )
        },
        bottomBar = {
            VoxBottomNav(
                currentTab = uiState.currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VoxBackground)
        ) {
            when (uiState.currentTab) {
                NavItem.WORKOUT -> {
                    WorkoutScreen(
                        currentExercise = uiState.selectedExercise,
                        allExercises = uiState.exercisesCatalog,
                        onSelectExercise = { viewModel.selectExercise(it) },
                        activeSets = uiState.activeSets,
                        voiceState = uiState.voiceState,
                        onMicPressDown = { viewModel.onMicPressDown() },
                        onMicPressUp = { viewModel.onMicPressUp() },
                        onQuickSampleClicked = { viewModel.applyVoiceSample(it) },
                        onAddRecognizedSet = { viewModel.addRecognizedSetToWorkout() },
                        onToggleSetCompleted = { viewModel.toggleSetCompleted(it) },
                        onEditSet = { viewModel.openEditSetDialog(it) },
                        onRemoveSet = { viewModel.removeSet(it) },
                        onRemoveLastSet = { viewModel.removeLastSet() },
                        onAddQuickSet = { weight, reps, isWarmup ->
                            viewModel.addQuickSet(weight, reps, isWarmup)
                        },
                        onOpenVoiceHelp = { viewModel.openVoiceHelpDialog() },
                        onFinishWorkoutClicked = { viewModel.showFinishWorkoutDialog() }
                    )
                }

                NavItem.HISTORY -> {
                    HistoryScreen(
                        historyList = uiState.workoutHistory,
                        onSelectSession = { viewModel.viewHistoryDetail(it) },
                        onStartNewWorkout = { viewModel.startNewWorkout() }
                    )
                }

                NavItem.EXERCISES -> {
                    ExercisesScreen(
                        exercises = uiState.filteredExercises,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSelectExercise = {
                            viewModel.selectExercise(it)
                            viewModel.selectTab(NavItem.WORKOUT)
                        },
                        onOpenAddExerciseDialog = { viewModel.openAddExerciseDialog() }
                    )
                }

                NavItem.SETTINGS -> {
                    SettingsScreen(
                        settings = uiState.settings,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onWeightUnitChange = { viewModel.setWeightUnit(it) },
                        onToggleOfflineMode = { viewModel.toggleOfflineMode(it) },
                        onToggleSound = { viewModel.toggleSoundFeedback(it) },
                        onToggleVibration = { viewModel.toggleVibration(it) },
                        onOpenVoiceHelp = { viewModel.openVoiceHelpDialog() },
                        onExportHistory = { viewModel.exportHistory() },
                        onOpenClearDataDialog = { viewModel.openClearDataDialog() }
                    )
                }
            }
        }
    }

    // Dialogs & Sheets
    if (uiState.showFinishDialog) {
        FinishWorkoutDialog(
            timerText = uiState.formattedTimer,
            totalVolumeKg = uiState.totalWorkoutVolumeKg,
            setsCount = uiState.activeSets.size,
            exerciseName = uiState.selectedExercise.name,
            onConfirmFinish = { viewModel.finishAndSaveWorkout() },
            onDismiss = { viewModel.hideFinishWorkoutDialog() }
        )
    }

    uiState.setToEdit?.let { setToEdit ->
        EditSetDialog(
            set = setToEdit,
            onSave = { weight, reps, isWarmup, isFailure ->
                viewModel.updateSetDetails(setToEdit.id, weight, reps, isWarmup, isFailure)
            },
            onDelete = { viewModel.removeSet(setToEdit.id) },
            onDismiss = { viewModel.closeEditSetDialog() }
        )
    }

    if (uiState.showVoiceHelpDialog) {
        VoiceHelpDialog(
            onDismiss = { viewModel.closeVoiceHelpDialog() }
        )
    }

    if (uiState.showAddExerciseDialog) {
        AddExerciseDialog(
            onConfirm = { name, category, muscles ->
                viewModel.addNewExercise(name, category, muscles)
            },
            onDismiss = { viewModel.closeAddExerciseDialog() }
        )
    }

    uiState.selectedHistorySession?.let { session ->
        WorkoutDetailsBottomSheet(
            session = session,
            onDismiss = { viewModel.viewHistoryDetail(null) }
        )
    }

    if (uiState.showExportDialog) {
        ExportDialog(
            history = uiState.workoutHistory,
            onDismiss = { viewModel.closeExportDialog() }
        )
    }

    if (uiState.showClearDataDialog) {
        ClearDataDialog(
            onConfirm = { viewModel.clearAllData() },
            onDismiss = { viewModel.closeClearDataDialog() }
        )
    }
}
