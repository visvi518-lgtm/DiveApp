package com.diveapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.diveapp.android.ui.AppRoot
import com.diveapp.android.ui.theme.DiveAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as DiveApplication).container

        setContent {
            DiveAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(container)
                }
            }
        }
    }
}
