package com.medtracking.app.data.scheduler

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.medtracking.app.MainActivity
import com.medtracking.app.domain.model.MealRelation
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class IntakeReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val CHANNEL_NAME = "İlaç Hatırlatıcıları"
        const val CHANNEL_DESCRIPTION = "İlaç alma zamanı geldiğinde bildirim gösterir"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val intakeId = intent.getLongExtra(AlarmReminderScheduler.EXTRA_INTAKE_ID, -1)
        val medicationName = intent.getStringExtra(AlarmReminderScheduler.EXTRA_MEDICATION_NAME) ?: "İlaç"
        val profileId = intent.getLongExtra(AlarmReminderScheduler.EXTRA_PROFILE_ID, -1)
        val plannedTimeStr = intent.getStringExtra(AlarmReminderScheduler.EXTRA_PLANNED_TIME)
        val mealRelationStr = intent.getStringExtra(AlarmReminderScheduler.EXTRA_MEAL_RELATION)

        if (intakeId == -1L) return

        // Saat bilgisini parse et
        val timeDisplay = try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            LocalTime.parse(plannedTimeStr?.substringAfter("T")?.take(5) ?: "00:00").format(formatter)
        } catch (e: Exception) {
            ""
        }
        
        // Kullanım talimatını parse et
        val mealRelation = MealRelation.fromString(mealRelationStr)
        val mealRelationText = mealRelation.toDisplayText()

        // Notification channel oluştur (Android 8+)
        createNotificationChannel(context)

        // Tıklandığında uygulamayı aç
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "today")
            putExtra("profile_id", profileId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            intakeId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Bildirim içeriğini oluştur (talimat varsa ekle)
        val contentText = if (mealRelationText.isNotEmpty()) {
            "$timeDisplay - $mealRelationText"
        } else {
            "$timeDisplay - İlacı almayı unutma!"
        }

        // Bildirimi oluştur
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("💊 $medicationName")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Bildirimi göster
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(intakeId.toInt(), notification)
            }
        } else {
            NotificationManagerCompat.from(context).notify(intakeId.toInt(), notification)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

