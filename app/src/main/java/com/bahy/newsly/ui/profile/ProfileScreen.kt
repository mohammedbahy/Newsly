package com.bahy.newsly.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import com.bahy.newsly.R
import com.bahy.newsly.di.AppModule
import com.bahy.newsly.ui.theme.Midnight
import com.bahy.newsly.ui.theme.NewslyTheme
import com.bahy.newsly.ui.theme.SplashBackground
import com.bahy.newsly.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel? = null,
    onLanguageClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val actualViewModel = viewModel ?: remember {
        ProfileViewModel(
            AppModule.provideAuthRepository(),
            AppModule.provideUserPreferencesRepository(context)
        )
    }
    val uiState by actualViewModel.uiState.collectAsState()
    val user = uiState.user

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            actualViewModel.setNotificationsEnabled(context, true)
        } else {
            actualViewModel.setNotificationsEnabled(context, false)
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Header
            Text(
                text = stringResource(id = R.string.profile),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color(0xFF000000),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Profile Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                ) {
                    val photoUrl = user?.profilePictureUrl
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Default avatar
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default profile picture",
                            tint = Color(0xFF000000).copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Name and Email
                Column {
                    Text(
                        text = user?.username ?: "Guest",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF000000)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF000000).copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Notifications Button
            SettingsButtonWithToggle(
                text = stringResource(id = R.string.notifications),
                checked = uiState.notificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                actualViewModel.setNotificationsEnabled(context, true)
                            }
                        } else {
                            actualViewModel.setNotificationsEnabled(context, true)
                        }
                    } else {
                        actualViewModel.setNotificationsEnabled(context, false)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Language Button
            SettingsButton(
                text = stringResource(id = R.string.language),
                onClick = onLanguageClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Country Button
            var showCountryDialog by remember { mutableStateOf(false) }
            SettingsButton(
                text = "Country: ${getCountryName(uiState.country)}",
                onClick = { showCountryDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            if (showCountryDialog) {
                CountrySelectionDialog(
                    currentCountry = uiState.country,
                    onCountrySelected = { country ->
                        actualViewModel.setCountry(country)
                        showCountryDialog = false
                    },
                    onDismiss = { showCountryDialog = false }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign Out Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp)
                    .clickable {
                        actualViewModel.signOut(context)
                        onSignOutClick()
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE0E0E0)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.sign_out),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF000000)
                    )
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFF000000).copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Navigation
            BottomNavigationBar(
                selectedIndex = 3,
                onHomeClick = onHomeClick,
                onCategoriesClick = onCategoriesClick,
                onBookmarkClick = onBookmarkClick,
                onProfileClick = {},
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    }
}

@Composable
private fun SettingsButtonWithToggle(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE0E0E0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF000000)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE0E0E0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF000000)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF000000).copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedIndex: Int,
    onHomeClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                isSelected = selectedIndex == 0,
                onClick = onHomeClick
            )
            BottomNavItem(
                icon = Icons.Default.GridView,
                isSelected = selectedIndex == 1,
                onClick = onCategoriesClick
            )
            BottomNavItem(
                icon = Icons.Default.BookmarkBorder,
                isSelected = selectedIndex == 2,
                onClick = onBookmarkClick
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                isSelected = selectedIndex == 3,
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Midnight else Midnight.copy(alpha = 0.5f),
        modifier = modifier
            .size(24.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun CountrySelectionDialog(
    currentCountry: String,
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val countries = listOf(
        "us" to "United States",
        "gb" to "United Kingdom",
        "ca" to "Canada",
        "au" to "Australia",
        "de" to "Germany",
        "fr" to "France",
        "it" to "Italy",
        "es" to "Spain",
        "nl" to "Netherlands",
        "be" to "Belgium",
        "se" to "Sweden",
        "no" to "Norway",
        "dk" to "Denmark",
        "fi" to "Finland",
        "pl" to "Poland",
        "pt" to "Portugal",
        "ie" to "Ireland",
        "ch" to "Switzerland",
        "at" to "Austria",
        "gr" to "Greece",
        "cz" to "Czech Republic",
        "ro" to "Romania",
        "hu" to "Hungary",
        "bg" to "Bulgaria",
        "hr" to "Croatia",
        "sk" to "Slovakia",
        "si" to "Slovenia",
        "ee" to "Estonia",
        "lv" to "Latvia",
        "lt" to "Lithuania",
        "jp" to "Japan",
        "kr" to "South Korea",
        "cn" to "China",
        "in" to "India",
        "sg" to "Singapore",
        "my" to "Malaysia",
        "th" to "Thailand",
        "ph" to "Philippines",
        "id" to "Indonesia",
        "vn" to "Vietnam",
        "nz" to "New Zealand",
        "za" to "South Africa",
        "eg" to "Egypt",
        "sa" to "Saudi Arabia",
        "ae" to "United Arab Emirates",
        "il" to "Israel",
        "tr" to "Turkey",
        "ru" to "Russia",
        "ua" to "Ukraine",
        "mx" to "Mexico",
        "br" to "Brazil",
        "ar" to "Argentina",
        "cl" to "Chile",
        "co" to "Colombia",
        "pe" to "Peru"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Country",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                items(countries) { (code, name) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onCountrySelected(code)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (code == currentCountry) 
                            Midnight.copy(alpha = 0.1f) 
                        else 
                            Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (code == currentCountry) 
                                    Midnight 
                                else 
                                    Color(0xFF000000),
                                fontWeight = if (code == currentCountry) 
                                    FontWeight.Bold 
                                else 
                                    FontWeight.Normal
                            )
                            if (code == currentCountry) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Selected",
                                    tint = Midnight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = Midnight
                )
            }
        }
    )
}

private fun getCountryName(countryCode: String): String {
    val countryMap = mapOf(
        "us" to "United States",
        "gb" to "United Kingdom",
        "ca" to "Canada",
        "au" to "Australia",
        "de" to "Germany",
        "fr" to "France",
        "it" to "Italy",
        "es" to "Spain",
        "nl" to "Netherlands",
        "be" to "Belgium",
        "se" to "Sweden",
        "no" to "Norway",
        "dk" to "Denmark",
        "fi" to "Finland",
        "pl" to "Poland",
        "pt" to "Portugal",
        "ie" to "Ireland",
        "ch" to "Switzerland",
        "at" to "Austria",
        "gr" to "Greece",
        "cz" to "Czech Republic",
        "ro" to "Romania",
        "hu" to "Hungary",
        "bg" to "Bulgaria",
        "hr" to "Croatia",
        "sk" to "Slovakia",
        "si" to "Slovenia",
        "ee" to "Estonia",
        "lv" to "Latvia",
        "lt" to "Lithuania",
        "jp" to "Japan",
        "kr" to "South Korea",
        "cn" to "China",
        "in" to "India",
        "sg" to "Singapore",
        "my" to "Malaysia",
        "th" to "Thailand",
        "ph" to "Philippines",
        "id" to "Indonesia",
        "vn" to "Vietnam",
        "nz" to "New Zealand",
        "za" to "South Africa",
        "eg" to "Egypt",
        "sa" to "Saudi Arabia",
        "ae" to "United Arab Emirates",
        "il" to "Israel",
        "tr" to "Turkey",
        "ru" to "Russia",
        "ua" to "Ukraine",
        "mx" to "Mexico",
        "br" to "Brazil",
        "ar" to "Argentina",
        "cl" to "Chile",
        "co" to "Colombia",
        "pe" to "Peru"
    )
    return countryMap[countryCode.lowercase()] ?: countryCode.uppercase()
}

@Preview(
    name = "Profile Screen",
    showBackground = true,
    backgroundColor = 0xFF6BB5B8,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait"
)
@Composable
private fun ProfileScreenPreview() {
    NewslyTheme {
        ProfileScreen()
    }
}

