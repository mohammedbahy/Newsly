package com.bahy.newsly.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bahy.newsly.R
import com.bahy.newsly.data.model.NewsArticle
import com.bahy.newsly.di.AppModule
import com.bahy.newsly.ui.theme.Midnight
import com.bahy.newsly.ui.theme.NewslyTheme
import com.bahy.newsly.ui.theme.SplashBackground
import com.bahy.newsly.ui.viewmodel.HomeViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = run {
        val context = LocalContext.current.applicationContext
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(
                        AppModule.provideNewsRepository(context),
                        AppModule.provideBookmarkRepository(AppModule.provideNewsRepository(context)),
                        AppModule.provideAuthRepository()
                    ) as T
                }
            }
        )
    },
    onNewsClick: (String) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onBookmarkNavClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    // Update search when query changes
    androidx.compose.runtime.LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            viewModel.searchArticles(searchQuery)
        } else {
            viewModel.loadNewsArticles(uiState.selectedCategory)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.browse),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Midnight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.discover_things),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Midnight.copy(alpha = 0.7f)
                )
            }

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    if (it.isEmpty()) {
                        viewModel.loadNewsArticles(uiState.selectedCategory)
                    }
                },
                onSearchClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryChip(
                        category = category,
                        onClick = { viewModel.selectCategory(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Cards
            if (uiState.isLoading && uiState.newsArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        color = Midnight.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(uiState.newsArticles) { article ->
                        NewsArticleCard(
                            article = article,
                            isBookmarked = uiState.bookmarkedArticleIds.contains(article.id),
                            onBookmarkClick = { viewModel.toggleBookmark(article.id) },
                            onClick = { onNewsClick(article.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recommended Section - Use weight to take remaining space
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.recommended_for_you),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Midnight
                    )
                    Text(
                        text = stringResource(id = R.string.see_all),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Midnight.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onSeeAllClick() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.recommendedArticles) { article ->
                        RecommendedArticleItem(
                            article = article,
                            isBookmarked = uiState.bookmarkedArticleIds.contains(article.id),
                            onBookmarkClick = { viewModel.toggleBookmark(article.id) },
                            onClick = { onNewsClick(article.id) }
                        )
                    }
                }
            }

            // Bottom Navigation
            BottomNavigationBar(
                selectedIndex = 0,
                onHomeClick = onHomeClick,
                onCategoriesClick = onCategoriesClick,
                onBookmarkClick = onBookmarkNavClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        }

        // Floating chat button
        // Draggable chat button
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        FloatingActionButton(
            onClick = onChatClick,
            containerColor = Midnight,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Default position like the screenshot: bottom-right but ABOVE bottom navigation
                .padding(end = 24.dp, bottom = 88.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Chat"
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(id = R.string.search),
                color = Midnight.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Midnight.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Midnight.copy(alpha = 0.6f),
                modifier = Modifier.clickable { onSearchClick() }
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Midnight,
            unfocusedTextColor = Midnight
        ),
        singleLine = true
    )
}

@Composable
private fun CategoryChip(
    category: com.bahy.newsly.data.model.Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (category.isSelected) Midnight else Color(0xFFF5F5F5)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (category.isSelected) Color.White else Midnight,
                fontWeight = if (category.isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun NewsArticleCard(
    article: NewsArticle,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // Article Image
        AsyncImage(
            model = article.imageUrl ?: "",
            contentDescription = article.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = androidx.compose.ui.graphics.painter.ColorPainter(Color(0xFFE0E0E0)),
            placeholder = androidx.compose.ui.graphics.painter.ColorPainter(Color(0xFFE0E0E0))
        )
        
        // Bookmark icon
        Icon(
            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clickable { onBookmarkClick() }
        )
        
        // Bottom overlay with category and title
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Text(
                text = article.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun RecommendedArticleItem(
    article: NewsArticle,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        AsyncImage(
            model = article.imageUrl ?: "",
            contentDescription = article.title,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            error = androidx.compose.ui.graphics.painter.ColorPainter(Color(0xFFE0E0E0)),
            placeholder = androidx.compose.ui.graphics.painter.ColorPainter(Color(0xFFE0E0E0))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = article.category,
                style = MaterialTheme.typography.bodySmall,
                color = Midnight.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = Midnight,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        
        // Bookmark icon
        Icon(
            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = Midnight.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { onBookmarkClick() }
        )
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
        color = Color.White,
        tonalElevation = 4.dp
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
    name = "Home Screen",
    showBackground = true,
    backgroundColor = 0xFF6BB5B8
)
@Composable
private fun HomeScreenPreview() {
    NewslyTheme {
        HomeScreen()
    }
}

