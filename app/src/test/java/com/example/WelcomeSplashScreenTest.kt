package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.screens.WelcomeSplashScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WelcomeSplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcomeSplashScreen_rendersExpectedContent() {
        var timedOut = false

        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomeSplashScreen(
                    titleEn = "Political Intelligence Desk",
                    titleAr = "مكتب الاستخبارات السياسية",
                    userName = "الدكتور بلال اللقيس",
                    welcomeNote = "نتمنى لك يومًا موفقًا في التحليل والمتابعة وصناعة القرار.",
                    durationMillis = 500L,
                    onTimeout = { timedOut = true }
                )
            }
        }

        // Verify root tag
        composeTestRule.onNodeWithTag("welcome_splash_screen").assertIsDisplayed()

        // Verify titles
        composeTestRule.onNodeWithText("POLITICAL INTELLIGENCE DESK").assertIsDisplayed()
        composeTestRule.onNodeWithText("مكتب الاستخبارات السياسية").assertIsDisplayed()

        // Verify user greeting
        composeTestRule.onNodeWithText("مرحبًا بك، الدكتور بلال اللقيس").assertIsDisplayed()
        composeTestRule.onNodeWithText("نتمنى لك يومًا موفقًا في التحليل والمتابعة وصناعة القرار.").assertIsDisplayed()

        // Wait until timeout callback fires
        composeTestRule.waitUntil(timeoutMillis = 2000L) { timedOut }
        assertTrue(timedOut)
    }
}
