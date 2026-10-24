package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleData
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SecuWipe", appName)
  }

  @Test
  fun `qr code matrix generates valid dimension`() {
    val matrix = QrCodeGenerator.generateMatrix("TEST-SECUWIPE-DATA")
    assertEquals(29, matrix.size)
    assertEquals(29, matrix[0].size)
    // Verify top-left finder pattern is populated
    assertTrue(matrix[0][0])
  }

  @Test
  fun `sample data has initial asset records`() {
    val devices = SampleData.sampleDevices
    assertTrue(devices.isNotEmpty())
    assertNotNull(devices.find { it.assetTag == "CLG-IT-2026-001" })
  }
}
