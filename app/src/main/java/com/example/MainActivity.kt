package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.AccountsViewModel
import com.example.ui.navigation.MainApp
import com.example.ui.theme.PrajapatiAccountsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AccountsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrajapatiAccountsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}
