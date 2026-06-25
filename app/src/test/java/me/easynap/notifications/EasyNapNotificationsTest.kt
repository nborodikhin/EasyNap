package me.easynap.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import me.easynap.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class EasyNapNotificationsTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        notificationManager = context.getSystemService(NotificationManager::class.java)
        shadowOf(notificationManager).setNotificationsEnabled(true)
    }

    @Test
    fun `alarm notifications unavailable when app notifications are disabled`() {
        assertFalse(
            EasyNapNotifications.alarmNotificationsAvailable(
                appNotificationsEnabled = false,
                alarmChannelImportance = NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    @Test
    fun `alarm notifications unavailable when alarm channel is blocked`() {
        assertFalse(
            EasyNapNotifications.alarmNotificationsAvailable(
                appNotificationsEnabled = true,
                alarmChannelImportance = NotificationManager.IMPORTANCE_NONE
            )
        )
    }

    @Test
    fun `alarm notifications unavailable when alarm channel is missing`() {
        assertFalse(
            EasyNapNotifications.alarmNotificationsAvailable(
                appNotificationsEnabled = true,
                alarmChannelImportance = null
            )
        )
    }

    @Test
    fun `alarm notifications available when app and alarm channel are enabled`() {
        assertTrue(
            EasyNapNotifications.alarmNotificationsAvailable(
                appNotificationsEnabled = true,
                alarmChannelImportance = NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    @Test
    fun `ensure channels creates timer and alarm channels`() {
        EasyNapNotifications.ensureChannels(context)

        val timerChannel = notificationManager.getNotificationChannel(EasyNapNotifications.CHANNEL_TIMER)
        val alarmChannel = notificationManager.getNotificationChannel(EasyNapNotifications.CHANNEL_ALARM)

        assertNotNull(timerChannel)
        assertEquals(context.getString(R.string.notification_channel_timer), timerChannel.name)
        assertEquals(NotificationManager.IMPORTANCE_LOW, timerChannel.importance)
        assertNull(timerChannel.sound)

        assertNotNull(alarmChannel)
        assertEquals(context.getString(R.string.notification_channel_alarm), alarmChannel.name)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, alarmChannel.importance)
        assertNull(alarmChannel.sound)
        assertEquals(Notification.VISIBILITY_PUBLIC, alarmChannel.lockscreenVisibility)
    }

    @Test
    fun `are alarm notifications available when app and alarm channel are enabled`() {
        EasyNapNotifications.ensureChannels(context)

        assertTrue(EasyNapNotifications.areAlarmNotificationsAvailable(context))
    }

    @Test
    fun `are alarm notifications unavailable when app notifications are disabled`() {
        EasyNapNotifications.ensureChannels(context)
        shadowOf(notificationManager).setNotificationsEnabled(false)

        assertFalse(EasyNapNotifications.areAlarmNotificationsAvailable(context))
    }

    @Test
    fun `are alarm notifications unavailable when alarm channel is blocked`() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                EasyNapNotifications.CHANNEL_ALARM,
                context.getString(R.string.notification_channel_alarm),
                NotificationManager.IMPORTANCE_NONE
            )
        )

        assertFalse(EasyNapNotifications.areAlarmNotificationsAvailable(context))
    }

    @Test
    fun `alarm notification settings intent opens alarm channel settings`() {
        val intent = EasyNapNotifications.alarmNotificationSettingsIntent(context)

        assertEquals(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS, intent.action)
        assertEquals(context.packageName, intent.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        assertEquals(
            EasyNapNotifications.CHANNEL_ALARM,
            intent.getStringExtra(Settings.EXTRA_CHANNEL_ID)
        )
    }

    @Test
    fun `app notification settings intent opens app notification settings`() {
        val intent = EasyNapNotifications.appNotificationSettingsIntent(context)

        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.action)
        assertEquals(context.packageName, intent.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        assertFalse(intent.hasExtra(Settings.EXTRA_CHANNEL_ID))
    }
}
