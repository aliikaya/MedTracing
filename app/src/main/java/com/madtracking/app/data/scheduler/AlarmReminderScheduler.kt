package com.madtracking.app.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.IntakeStatus
import com.madtracking.app.domain.scheduler.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {

    private val alarmManager: AlarmManager = 
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "reminder_prefs"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        const val EXTRA_INTAKE_ID = "intake_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_PROFILE_ID = "profile_id"
        const val EXTRA_PLANNED_TIME = "planned_time"
    }

    override fun scheduleIntakeReminder(intake: Intake, medicationName: String) {
        // Hatırlatıcılar kapalıysa çık
        if (!areRemindersEnabled()) return

        // Zaten alınmış veya kaçırılmış ise hatırlatma kurma
        if (intake.status != IntakeStatus.PLANNED) return

        // Geçmiş zaman için hatırlatma kurma
        val triggerTimeMillis = intake.plannedTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        if (triggerTimeMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, IntakeReminderReceiver::class.java).apply {
            putExtra(EXTRA_INTAKE_ID, intake.id)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_PROFILE_ID, intake.profileId)
            putExtra(EXTRA_PLANNED_TIME, intake.plannedTime.toString())
        }

        val requestCode = getRequestCode(intake.id)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Exact alarm kullan (ilaç hatırlatması kritik)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                // Exact alarm izni yoksa normal alarm kullan
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    override fun cancelIntakeReminder(intakeId: Long) {
        val intent = Intent(context, IntakeReminderReceiver::class.java)
        val requestCode = getRequestCode(intakeId)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    override fun cancelAllForMedication(medicationId: Long) {
        // Not: Bu basit implementasyonda tüm Intake ID'lerini bilmiyoruz.
        // Gerçek uygulamada IntakeRepository'den ilgili Intake'leri çekip
        // her birini iptal etmek gerekir.
        // Şimdilik bu metod placeholder olarak kalacak.
        // İlaç silindiğinde Intake'ler de cascade ile silinecek
        // ve yeni alarm kurulmayacak.
    }

    override fun areRemindersEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
    }

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
    }

    private fun getRequestCode(intakeId: Long): Int {
        // Long'u Int'e güvenli şekilde dönüştür
        return (intakeId % Int.MAX_VALUE).toInt()
    }
}

