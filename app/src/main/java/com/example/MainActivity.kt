package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.CreativePaintTheme
import com.example.viewmodel.DrawingViewModel
import com.example.viewmodel.GalleryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val drawingViewModel: DrawingViewModel = viewModel()
            val galleryViewModel: GalleryViewModel = viewModel()
            val themeMode by drawingViewModel.userPrefs.themeMode.collectAsStateWithLifecycle()

            CreativePaintTheme(themePreference = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        drawingViewModel = drawingViewModel,
                        galleryViewModel = galleryViewModel
                    )
                }
            }
        }
    }
}

object NavDestinations {
    const val HOME = "home"
    const val WORKSPACE = "workspace"
    const val GALLERY = "gallery"
    const val EXPORT = "export"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(
    drawingViewModel: DrawingViewModel,
    galleryViewModel: GalleryViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavDestinations.HOME
    ) {
        composable(NavDestinations.HOME) {
            HomeScreen(
                galleryViewModel = galleryViewModel,
                onNavigateToWorkspace = { projectId ->
                    drawingViewModel.loadProject(projectId)
                    navController.navigate(NavDestinations.WORKSPACE)
                },
                onNavigateToNewCanvas = { title, width, height, bgColor ->
                    drawingViewModel.initNewCanvas(title, width, height, bgColor)
                    navController.navigate(NavDestinations.WORKSPACE)
                },
                onNavigateToGallery = {
                    navController.navigate(NavDestinations.GALLERY)
                },
                onNavigateToSettings = {
                    navController.navigate(NavDestinations.SETTINGS)
                }
            )
        }

        composable(NavDestinations.WORKSPACE) {
            WorkspaceScreen(
                viewModel = drawingViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToExport = {
                    navController.navigate(NavDestinations.EXPORT)
                }
            )
        }

        composable(NavDestinations.GALLERY) {
            GalleryScreen(
                galleryViewModel = galleryViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenProject = { projectId ->
                    drawingViewModel.loadProject(projectId)
                    navController.navigate(NavDestinations.WORKSPACE)
                },
                onNavigateToNewCanvas = { title, width, height, bgColor ->
                    drawingViewModel.initNewCanvas(title, width, height, bgColor)
                    navController.navigate(NavDestinations.WORKSPACE)
                }
            )
        }

        composable(NavDestinations.EXPORT) {
            ExportScreen(
                viewModel = drawingViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavDestinations.SETTINGS) {
            SettingsScreen(
                userPrefs = drawingViewModel.userPrefs,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
