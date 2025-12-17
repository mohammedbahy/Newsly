package com.bahy.newsly.ui.articledetail

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.bahy.newsly.data.model.NewsArticle
import com.bahy.newsly.di.AppModule
import com.bahy.newsly.ui.theme.Midnight
import com.bahy.newsly.ui.theme.SplashBackground
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ArticleDetailScreen(
    articleId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current.applicationContext
    val newsRepository = AppModule.provideNewsRepository(context)
    val bookmarkRepository = AppModule.provideBookmarkRepository(
        AppModule.provideNewsRepository(context)
    )
    val authRepository = AppModule.provideAuthRepository()

    var article by remember { 
        mutableStateOf<NewsArticle?>(null) 
    }
    var isLoading by remember { 
        mutableStateOf(true) 
    }
    var isBookmarked by remember { 
        mutableStateOf(false) 
    }
    var userId by remember { 
        mutableStateOf<String?>(null) 
    }

    // Load article
    LaunchedEffect(articleId) {
        isLoading = true
        try {
            val loadedArticle = newsRepository.getArticleById(articleId)
            // If article is null or doesn't have a valid URL, go back
            if (loadedArticle == null || loadedArticle.url.isNullOrBlank()) {
                onBackClick()
                return@LaunchedEffect
            }
            article = loadedArticle
            isLoading = false
        } catch (_: Exception) {
            isLoading = false
            // If error loading article, go back
            onBackClick()
        }
    }

    // Load user and bookmark status
    LaunchedEffect(articleId, article) {
        if (article != null) {
            val user = authRepository.currentUser.firstOrNull()
            userId = user?.id
            if (userId != null) {
                isBookmarked = bookmarkRepository.isBookmarked(userId!!, articleId)
            }
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
            // Header with back button and bookmark
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Midnight
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(
                        onClick = {
                            if (userId != null && article != null) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (isBookmarked) {
                                        bookmarkRepository.removeBookmark(userId!!, articleId)
                                        isBookmarked = false
                                    } else {
                                        bookmarkRepository.addBookmark(userId!!, article!!)
                                        isBookmarked = true
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                            tint = Midnight
                        )
                    }
                }
            }

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Midnight)
                }
            } else if (article == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Article not found",
                        color = Midnight.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val articleUrl = article!!.url
                if (articleUrl != null) {
                    // Use WebView to display full article
                    var isLoadingWebView by remember { mutableStateOf(true) }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(SplashBackground)
                    ) {
                        // Article Header Info
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Title
                                Text(
                                    text = article!!.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Midnight,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Source and Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = article!!.source,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Midnight.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = formatDate(article!!.publishedAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Midnight.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // WebView to display full article
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isLoadingWebView = false
                                            }
                                        }
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        loadUrl(articleUrl)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isLoadingWebView) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Midnight)
                                }
                            }
                        }
                    }
                } else {
                    // Fallback: Display article content if URL is not available
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .background(SplashBackground)
                    ) {
                        // Article Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            AsyncImage(
                                model = article!!.imageUrl ?: "",
                                contentDescription = article!!.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = androidx.compose.ui.graphics.painter.ColorPainter(Color(0xFFE0E0E0))
                            )

                            // Category badge
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Midnight.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = article!!.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Article Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Title
                            Text(
                                text = article!!.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = Midnight,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Source and Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = article!!.source,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Midnight.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = formatDate(article!!.publishedAt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Midnight.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Description
                            if (!article!!.description.isNullOrBlank()) {
                                Text(
                                    text = article!!.description,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 18.sp,
                                        lineHeight = 28.sp
                                    ),
                                    color = Midnight.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // Content
                            val articleContent = article!!.content
                            if (!articleContent.isNullOrBlank()) {
                                Text(
                                    text = articleContent,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 26.sp
                                    ),
                                    color = Midnight.copy(alpha = 0.9f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Justify
                                )
                            } else {
                                Text(
                                    text = "Content not available.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 26.sp
                                    ),
                                    color = Midnight.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
            sdf.format(date)
        }
    }
}

