package com.sigolaev.lab2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.sigolaev.lab2.ui.NoteScreen
import com.sigolaev.lab2.ui.theme.Sigolaev_AI234_Lab2Theme
import com.sigolaev.lab2.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sigolaev_AI234_Lab2Theme {
                NoteScreen(viewModel = viewModel)
            }
        }
    }
}