package com.mahendra.android.hydrolinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mahendra.android.hydrolinx.core.navigation.HydrolinxNavHost
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HydrolinxTheme {
                HydrolinxNavHost()
            }
        }
    }
}
