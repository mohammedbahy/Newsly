package com.bahy.newsly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bahy.newsly.ui.forgotpassword.ForgotPasswordScreen
import com.bahy.newsly.ui.theme.NewslyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgotPasswordScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test
    fun forgotPasswordPageTest() {
        // Render the ForgotPasswordScreen widget
        composeTestRule.setContent {
            NewslyTheme {
                ForgotPasswordScreen()
            }
        }

        // Wait for UI to be ready
        composeTestRule.waitForIdle()

        // Verify UI elements are present
        composeTestRule.onNodeWithText("Forgot Password?")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Enter your email and we will send you a link to reset your password.")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Send Reset Link")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Back to Sign In")
            .assertIsDisplayed()

        // Interact with the email field - find by placeholder text
        composeTestRule.onNodeWithText("Email Address")
            .performTextInput("test@example.com")
        
        // Verify the text was entered
        composeTestRule.onNodeWithText("test@example.com")
            .assertIsDisplayed()
        
        // Tap on the Send Reset Link button
        composeTestRule.onNodeWithText("Send Reset Link")
            .performClick()
        
        // Wait for UI to update
        composeTestRule.waitForIdle()

        // Verify result - Button should still be displayed after click
        composeTestRule.onNodeWithText("Send Reset Link")
            .assertIsDisplayed()
        
        // Test Result: All UI elements displayed correctly, email input works, button is clickable
        println("✅ Test Result: Forgot Password Screen Test PASSED")
        println("   - All UI elements are displayed")
        println("   - Email field accepts input")
        println("   - Send Reset Link button is clickable")
        println("   - Back to Sign In button is displayed")
    }
}

