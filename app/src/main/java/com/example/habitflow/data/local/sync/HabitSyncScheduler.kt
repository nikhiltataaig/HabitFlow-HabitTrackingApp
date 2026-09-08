package com.example.habitflow.data.local.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class HabitSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun schedule(userId: String) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                NetworkType.CONNECTED
            )
            .build()

        val request =
            OneTimeWorkRequestBuilder<HabitSyncWorker>()
                .setInputData(
                    workDataOf(
                        HabitSyncWorker.KEY_USER_ID to userId
                    )
                )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "habit_sync_$userId",
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    fun scheduleRemoteToLocalSync(
        userId: String
    ) {

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val request =
            OneTimeWorkRequestBuilder<RemoteToLocalSyncWorker>()
                .setInputData(
                    workDataOf(
                        RemoteToLocalSyncWorker.KEY_USER_ID
                                to userId
                    )
                )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "remote_to_local_$userId",
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    fun scheduleInitialSync(userId: String) {
        scheduleRemoteToLocalSync(userId)
        schedule(userId)
    }



}