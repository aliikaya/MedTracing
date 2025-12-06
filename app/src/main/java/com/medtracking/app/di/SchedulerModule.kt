package com.medtracking.app.di

import com.medtracking.app.data.scheduler.AlarmReminderScheduler
import com.medtracking.app.domain.scheduler.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        alarmReminderScheduler: AlarmReminderScheduler
    ): ReminderScheduler
}

