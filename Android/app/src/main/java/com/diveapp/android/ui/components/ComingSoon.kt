package com.diveapp.android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/** Placeholder for tabs whose real screens land in Phase 4
 * (Docs/07_Screens.md), so the tab structure is testable end to end now. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(title: String, icon: ImageVector = Icons.Filled.Build) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
    ) { padding ->
        EmptyStateView(
            title = title,
            message = "다음 단계에서 구현될 화면입니다.",
            icon = icon,
            modifier = Modifier.padding(padding),
        )
    }
}
