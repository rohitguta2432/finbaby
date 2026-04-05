package com.finbaby.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.finbaby.app.navigation.FinBabyNavGraph
import com.finbaby.app.ui.theme.FinBabyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinBabyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinBabyNavGraph()
                }
            }
        }
    }
}
