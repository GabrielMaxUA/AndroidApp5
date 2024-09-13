package com.trios2024aa.itunes


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trios2024aa.itunes.ui.theme.ITunesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ITunesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}


@Composable
fun MainScreen(
    repository: ITunesRepository = ITunesRepository(itunesService, RssFeedService.instance),
    viewModel: ITunesViewModel = viewModel(factory = ITunesViewModelFactory(repository))
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        // Composable for the list of items
        composable("list") {
            ItemListScreen(viewModel, navController)
        }

        // Composable for the item detail screen
        composable("details/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            // Pass the selected item ID to the detail screen
            itemId?.let {
                ItemDetailScreen(viewModel, itemId, navController)
            }
        }
    }
}