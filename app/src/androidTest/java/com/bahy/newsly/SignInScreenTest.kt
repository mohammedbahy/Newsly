package com.bahy.newsly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bahy.newsly.ui.signin.SignInScreen
import com.bahy.newsly.ui.theme.NewslyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
    @Test
    fun signInPageTest() {
        // Render the SignInScreen widget
        composeTestRule.setContent {
            NewslyTheme {
                SignInScreen()
            }
        }
        // Wait for UI to be ready
        composeTestRule.waitForIdle()
        // Verify main UI elements are present
        composeTestRule.onNodeWithText("Welcome Back")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("I am happy to see you again. You can continue where you left off by logging in")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In with Google")
            .assertIsDisplayed()
        // Click on Forgot Password link
        composeTestRule.onNodeWithText("Forgot Password?")
            .performClick()
        // Wait for UI to update
        composeTestRule.waitForIdle()
        // Tap on the Sign In button
        composeTestRule.onNodeWithText("Sign In")
            .performClick()
        // Wait for UI to update
        composeTestRule.waitForIdle()
        // Verify result - Button should still be displayed after click
        composeTestRule.onNodeWithText("Sign In")
            .assertIsDisplayed()
        // Test Result
        println("✅ Test Result: Sign In Screen Test PASSED")
        println("   - Welcome Back title is displayed")
        println("   - Sign In button is displayed and clickable")
        println("   - Forgot Password link is clickable")
        println("   - Sign In with Google button is displayed")
    }
}

