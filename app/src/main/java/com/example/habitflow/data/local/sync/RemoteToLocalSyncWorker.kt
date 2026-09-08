package com.example.habitflow.data.local.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RemoteToLocalSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val remoteToLocalSync: RemoteToLocalSync
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {

        val userId =
            inputData.getString(KEY_USER_ID)
                ?: return Result.failure()

        val result =
            remoteToLocalSync.sync(userId)

        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val KEY_USER_ID = "user_id"
    }
}