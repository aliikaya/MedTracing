package com.medtracking.app.data.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Cihaz yeniden başladığında alarmları yeniden kurmak için receiver.
 * Şimdilik placeholder - ileride alarm'ları yeniden planlama mantığı eklenecek.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Uygulamayı başlat ve tüm aktif ilaçlar için alarmları yeniden planla
            // Bu işlem için WorkManager kullanılabilir
        }
    }
}

