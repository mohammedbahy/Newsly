package com.bahy.newsly.ui.categories

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahy.newsly.R
import com.bahy.newsly.ui.theme.Midnight
import com.bahy.newsly.ui.theme.NewslyTheme
import com.bahy.newsly.ui.theme.SplashBackground

data class CategoryItem(
    val id: String,
    val name: String,
    val emoji: String
)

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val sportsText = stringResource(id = R.string.sports)
    val politicsText = stringResource(id = R.string.politics)
    val lifeText = stringResource(id = R.string.life)
    val gamingText = stringResource(id = R.string.gaming)
    val animalsText = stringResource(id = R.string.animals)
    val natureText = stringResource(id = R.string.nature)
    val foodText = stringResource(id = R.string.food)
    val artText = stringResource(id = R.string.art)
    val historyText = stringResource(id = R.string.history)
    val fashionText = stringResource(id = R.string.fashion)
    val covidText = stringResource(id = R.string.covid_19)
    val middleEastText = stringResource(id = R.string.middle_east)
    
    val categories = remember(sportsText, politicsText, lifeText, gamingText, animalsText, natureText, foodText, artText, historyText, fashionText, covidText, middleEastText) {
        listOf(
            CategoryItem("1", sportsText, "⚽"),
            CategoryItem("2", politicsText, "⚖️"),
            CategoryItem("3", lifeText, "😊"),
            CategoryItem("4", gamingText, "🎮"),
            CategoryItem("5", animalsText, "🐻"),
            CategoryItem("6", natureText, "🌴"),
            CategoryItem("7", foodText, "🍔"),
            CategoryItem("8", artText, "🎨"),
            CategoryItem("9", historyText, "📜"),
            CategoryItem("10", fashionText, "👗"),
            CategoryItem("11", covidText, "😷"),
            CategoryItem("12", middleEastText, "⚔️")
        )
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.categories),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.categories_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF000000).copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.name) }
                    )
                }
            }

            // Bottom Navigation
            BottomNavigationBar(
                selectedIndex = 1,
                onHomeClick = onHomeClick,
                onCategoriesClick = {},
                onBookmarkClick = onBookmarkClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.emoji,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFF000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

@Preview(
    name = "Categories Screen",
    showBackground = true,
    backgroundColor = 0xFF6BB5B8
)
@Composable
private fun CategoriesScreenPreview() {
    NewslyTheme {
        CategoriesScreen()
    }
}

