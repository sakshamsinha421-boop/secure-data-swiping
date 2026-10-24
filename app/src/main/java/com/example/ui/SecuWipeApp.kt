package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CertificatesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DataWipingScreen
import com.example.ui.screens.DeviceHistoryScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.StatusPass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuWipeApp(viewModel: SecuWipeViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Custom back handling: if in sub-screen, back returns to Dashboard
    if (currentDestination != AppDestination.DASHBOARD) {
        BackHandler {
            viewModel.navigateTo(AppDestination.DASHBOARD)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberNavyBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // Tablet & Desktop Responsive Layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                CyberNavigationRail(
                    currentDestination = currentDestination,
                    onDestinationSelected = { viewModel.navigateTo(it) }
                )

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = CyberNavyBg,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CyberTopAppBar(
                            title = currentDestination.title,
                            showMenuButton = false,
                            onMenuClick = {}
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ScreenContent(
                            destination = currentDestination,
                            viewModel = viewModel,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        } else {
            // Phone / Compact screen with Drawer and Bottom Navigation
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = CyberSurface,
                        drawerContentColor = TextPrimary
                    ) {
                        CyberDrawerHeader()
                        Spacer(modifier = Modifier.height(8.dp))
                        AppDestination.values().forEach { destination ->
                            CyberDrawerItem(
                                destination = destination,
                                isSelected = currentDestination == destination,
                                onClick = {
                                    viewModel.navigateTo(destination)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    containerColor = CyberNavyBg,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CyberTopAppBar(
                            title = currentDestination.title,
                            showMenuButton = true,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    },
                    bottomBar = {
                        CyberBottomNavigationBar(
                            currentDestination = currentDestination,
                            onDestinationSelected = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ScreenContent(
                            destination = currentDestination,
                            viewModel = viewModel,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    destination: AppDestination,
    viewModel: SecuWipeViewModel,
    onNavigate: (AppDestination) -> Unit
) {
    when (destination) {
        AppDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel, onNavigate = onNavigate)
        AppDestination.REGISTER_DEVICE -> RegistrationScreen(viewModel = viewModel, onNavigate = onNavigate)
        AppDestination.DATA_WIPING -> DataWipingScreen(viewModel = viewModel, onNavigate = onNavigate)
        AppDestination.VERIFICATION -> VerificationScreen(viewModel = viewModel, onNavigate = onNavigate)
        AppDestination.CERTIFICATES -> CertificatesScreen(viewModel = viewModel, onNavigate = onNavigate)
        AppDestination.DEVICE_HISTORY -> DeviceHistoryScreen(viewModel = viewModel, onNavigate = onNavigate)
        AppDestination.SETTINGS -> SettingsScreen(viewModel = viewModel, onNavigate = onNavigate)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTopAppBar(
    title: String,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SECUWIPE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        },
        navigationIcon = {
            if (showMenuButton) {
                IconButton(onClick = onMenuClick, modifier = Modifier.testTag("btn_open_drawer")) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = CyberCyan)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CyberNavyBg,
            titleContentColor = TextPrimary
        )
    )
}

@Composable
fun CyberNavigationRail(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    NavigationRail(
        containerColor = CyberSurface,
        contentColor = TextSecondary,
        modifier = Modifier
            .fillMaxHeight()
            .border(width = 0.5.dp, color = CyberBorderSubtle)
            .testTag("nav_rail")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurfaceVariant)
                .border(1.dp, CyberCyan, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))

        AppDestination.values().forEach { destination ->
            val isSelected = currentDestination == destination
            NavigationRailItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = getDestinationIcon(destination),
                        contentDescription = destination.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = CyberCyan,
                    selectedTextColor = CyberCyan,
                    indicatorColor = CyberCyan.copy(alpha = 0.15f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_rail_item_${destination.name}")
            )
        }
    }
}

@Composable
fun CyberBottomNavigationBar(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    // Show 4 primary destinations in bottom bar
    val primaryDestinations = listOf(
        AppDestination.DASHBOARD,
        AppDestination.DATA_WIPING,
        AppDestination.VERIFICATION,
        AppDestination.CERTIFICATES
    )

    NavigationBar(
        containerColor = CyberSurface,
        contentColor = TextSecondary,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(width = 0.5.dp, color = CyberBorderSubtle)
            .testTag("bottom_nav_bar")
    ) {
        primaryDestinations.forEach { dest ->
            val isSelected = currentDestination == dest
            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(dest) },
                icon = {
                    Icon(
                        imageVector = getDestinationIcon(dest),
                        contentDescription = dest.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = dest.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberCyan,
                    selectedTextColor = CyberCyan,
                    indicatorColor = CyberCyan.copy(alpha = 0.15f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("bottom_nav_${dest.name}")
            )
        }
    }
}

@Composable
fun CyberDrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurfaceVariant)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberCyan, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "SECUWIPE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "IT Asset Sanitization & Recycling",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "College IT Mini Project Prototype",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
        )
    }
}

@Composable
fun CyberDrawerItem(
    destination: AppDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) CyberCyan.copy(alpha = 0.15f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("drawer_item_${destination.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getDestinationIcon(destination),
                contentDescription = null,
                tint = if (isSelected) CyberCyan else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = destination.title,
                color = if (isSelected) CyberCyan else TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

fun getDestinationIcon(destination: AppDestination): ImageVector {
    return when (destination) {
        AppDestination.DASHBOARD -> Icons.Default.Dashboard
        AppDestination.REGISTER_DEVICE -> Icons.Default.AddCircle
        AppDestination.DATA_WIPING -> Icons.Default.DeleteSweep
        AppDestination.VERIFICATION -> Icons.Default.FactCheck
        AppDestination.CERTIFICATES -> Icons.Default.Verified
        AppDestination.DEVICE_HISTORY -> Icons.Default.History
        AppDestination.SETTINGS -> Icons.Default.Settings
    }
}
