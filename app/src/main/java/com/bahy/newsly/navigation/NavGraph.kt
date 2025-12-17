package com.bahy.newsly.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bahy.newsly.ui.articledetail.ArticleDetailScreen
import com.bahy.newsly.ui.bookmarks.BookmarksScreen
import com.bahy.newsly.ui.categories.CategoriesScreen
import com.bahy.newsly.ui.categoryarticles.CategoryArticlesScreen
import com.bahy.newsly.ui.home.HomeScreen
import com.bahy.newsly.ui.profile.ProfileScreen
import com.bahy.newsly.ui.signin.SignInScreen
import com.bahy.newsly.ui.signup.SignUpScreen
import com.bahy.newsly.ui.splash.SplashScreen
import com.bahy.newsly.ui.forgotpassword.ForgotPasswordScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Categories : Screen("categories")
    object CategoryArticles : Screen("category_articles/{categoryName}") {
        fun createRoute(categoryName: String) = "category_articles/$categoryName"
    }
    object ArticleDetail : Screen("article_detail/{articleId}") {
        fun createRoute(articleId: String) = "article_detail/$articleId"
    }
    object Bookmarks : Screen("bookmarks")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToSignIn = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Home.route) {
            val context = LocalContext.current
            HomeScreen(
                onNewsClick = { newsId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(newsId))
                },
                onSeeAllClick = {
                    // TODO: Navigate to recommended list screen
                },
                onCategoriesClick = {
                    navController.navigate(Screen.Categories.route)
                },
                onHomeClick = {
                    // Already on home
                },
                onBookmarkNavClick = {
                    navController.navigate(Screen.Bookmarks.route)
                },
                onChatClick = {
                    navController.navigate(Screen.Chat.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onCategoryClick = { categoryName ->
                    navController.navigate(Screen.CategoryArticles.createRoute(categoryName))
                },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Categories.route) { inclusive = true }
                    }
                },
                onBookmarkClick = {
                    navController.navigate(Screen.Bookmarks.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(
            route = Screen.CategoryArticles.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryArticlesScreen(
                categoryName = categoryName,
                onBackClick = {
                    navController.popBackStack()
                },
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            ArticleDetailScreen(
                articleId = articleId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                onBookmarkClick = { bookmarkId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(bookmarkId))
                },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Bookmarks.route) { inclusive = true }
                    }
                },
                onCategoriesClick = {
                    navController.navigate(Screen.Categories.route) {
                        popUpTo(Screen.Bookmarks.route) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOutClick = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                },
                onCategoriesClick = {
                    navController.navigate(Screen.Categories.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                },
                onBookmarkClick = {
                    navController.navigate(Screen.Bookmarks.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Chat.route) {
            com.bahy.newsly.ui.chat.ChatScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

