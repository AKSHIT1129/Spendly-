package com.example
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.screens.SpendlyDashboard
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {
  @get:Rule val composeTestRule = createComposeRule()
  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(
          color = MaterialTheme.colorScheme.background
        ) {
          Box(
            modifier = Modifier
              .padding(24.dp)
              .fillMaxWidth()
          ) {
            Text(
              text = "Welcome to Spendly!",
              color = MaterialTheme.colorScheme.primary,
              style = MaterialTheme.typography.headlineMedium
            )
          }
        }
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
  @Test
  fun test_full_dashboard_render() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(app)
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(
          color = MaterialTheme.colorScheme.background
        ) {
          SpendlyDashboard(viewModel = viewModel)
        }
      }
    }
    composeTestRule.waitForIdle()
  }
}
