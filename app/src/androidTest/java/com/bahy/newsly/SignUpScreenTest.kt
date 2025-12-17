package com.bahy.newsly

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bahy.newsly.ui.signup.SignUpScreen
import com.bahy.newsly.ui.theme.NewslyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
    @Test
    fun signUpPageTest() {
        // Render the SignUpScreen widget
        composeTestRule.setContent {
            NewslyTheme {
                SignUpScreen()
            }
        }
        // Wait for UI to be ready
        composeTestRule.waitForIdle()
        // Verify main UI elements are present
        // "Register" title should be displayed
        val registerNodes = composeTestRule.onAllNodesWithText("Register")
        registerNodes[0].assertIsDisplayed()
        // Verify Register button exists
        val registerButton = registerNodes[1]
        registerButton.assertIsDisplayed()
        // Verify terms text is displayed
        composeTestRule.onNodeWithText("Agree to Our terms and conditions")
            .assertIsDisplayed()
        // Click on the terms checkbox
        composeTestRule.onNodeWithText("Agree to Our terms and conditions")
            .performClick()
        // Wait for UI to update
        composeTestRule.waitForIdle()
        // Tap on the Register button
        registerButton.performClick()
        // Wait for UI to update
        composeTestRule.waitForIdle()
        // Verify result - Button should still be displayed after click
        composeTestRule.onAllNodesWithText("Register")[1]
            .assertIsDisplayed()
        // Test Result
        println("✅ Test Result: Sign Up Screen Test PASSED")
        println("   - Register title is displayed")
        println("   - Register button is displayed and clickable")
        println("   - Terms checkbox is clickable")
    }
}

