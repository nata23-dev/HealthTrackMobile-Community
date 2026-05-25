package com.example.healthtrackmobile.receiver

import android.content.Context
import androidx.work.*
import com.example.healthtrackmobile.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncRemindersWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = inputData.getString("USER_ID") ?: return@withContext Result.failure()
        
        return@withContext try {
            ReminderSyncManager.syncReminders(context, userId)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "sync_reminders_work"

        fun schedule(context: Context, userId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncRemindersWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(workDataOf("USER_ID" to userId))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
