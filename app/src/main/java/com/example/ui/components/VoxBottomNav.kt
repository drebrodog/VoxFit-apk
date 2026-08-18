package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavItem
import com.example.ui.theme.VoxBackground
import com.example.ui.theme.VoxBlue
import com.example.ui.theme.VoxBorder
import com.example.ui.theme.VoxSurface
import com.example.ui.theme.VoxSurfaceVariant
import com.example.ui.theme.VoxTextMuted
import com.example.ui.theme.VoxTextPrimary

@Composable
fun VoxBottomNav(
    currentTab: NavItem,
    onTabSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = VoxBorder)
            .background(VoxSurface),
        containerColor = VoxSurface,
        tonalElevation = 8.dp
    ) {
        NavItem.entries.forEach { item ->
            val isSelected = currentTab == item
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VoxBlue,
                    selectedTextColor = VoxBlue,
                    unselectedIconColor = VoxTextMuted,
                    unselectedTextColor = VoxTextMuted,
                    indicatorColor = VoxBlue.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
