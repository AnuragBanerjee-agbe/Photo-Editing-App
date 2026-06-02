package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.VideoEditorDatabase
import com.example.data.repository.VideoEditorRepository
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VideoEditorViewModel
import com.example.ui.viewmodel.VideoEditorViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = VideoEditorDatabase.getDatabase(applicationContext)
    val repository = VideoEditorRepository(database.videoEditorDao())
    val viewModel = ViewModelProvider(
      this,
      VideoEditorViewModelFactory(application, repository)
    )[VideoEditorViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          VideoEditorScreen(viewModel)
        }
      }
    }
  }
}

