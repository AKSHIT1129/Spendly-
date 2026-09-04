package com.example
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.FinanceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {
  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Spendly", appName)
  }
  @Test
  fun `test finance viewmodel initialization`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(app)
    assertNotNull(viewModel)
  }
  @Test
  fun `test essential category detection`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(app)
    assertEquals(true, viewModel.isCategoryEssentialByDefault("Rent"))
    assertEquals(true, viewModel.isCategoryEssentialByDefault("Food"))
    assertEquals(true, viewModel.isCategoryEssentialByDefault("Utilities"))
    assertEquals(false, viewModel.isCategoryEssentialByDefault("Entertainment"))
    assertEquals(false, viewModel.isCategoryEssentialByDefault("Shopping"))
  }
}
