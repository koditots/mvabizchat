package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("MVA Business Chat", appName)
  }

  @Test
  fun `user presence active within threshold is online`() {
    val presence = com.example.data.model.UserPresence(
      email = "merversaus@gmail.com",
      isOnline = true,
      lastActive = System.currentTimeMillis() - 10_000L // 10s ago
    )
    org.junit.Assert.assertTrue(presence.isCurrentlyOnline())
    assertEquals("Online", presence.formatLastActiveText())
  }

  @Test
  fun `user presence expired threshold is offline`() {
    val presence = com.example.data.model.UserPresence(
      email = "merversaus@gmail.com",
      isOnline = true,
      lastActive = System.currentTimeMillis() - 300_000L // 5m ago
    )
    org.junit.Assert.assertFalse(presence.isCurrentlyOnline())
    assertEquals("5m ago", presence.formatLastActiveText())
  }

  @Test
  fun `user presence explicitly marked offline`() {
    val presence = com.example.data.model.UserPresence(
      email = "merversaus@gmail.com",
      isOnline = false,
      lastActive = System.currentTimeMillis() - 1000L
    )
    org.junit.Assert.assertFalse(presence.isCurrentlyOnline())
    assertEquals("just now", presence.formatLastActiveText())
  }
}
