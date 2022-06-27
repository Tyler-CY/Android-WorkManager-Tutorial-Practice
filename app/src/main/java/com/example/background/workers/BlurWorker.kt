package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        // Get a Context by calling the applicationContext property. Assign it to a new val named appContext. You'll need this for various bitmap manipulations you're about to do.
        val appContext = applicationContext

        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        // Display a status notification using the function, makeStatusNotification to notify the user about blurring the image.
        makeStatusNotification("Blurring image", appContext)

        sleep()

        return try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

            // Get a blurred version of the bitmap by calling the blurBitmap method from WorkerUtils.
            val output = blurBitmap(picture, appContext)

            // Write that bitmap to a temporary file by calling the writeBitmapToFile method from WorkerUtils. Make sure to save the returned URI to a local variable.
            val outputUri = writeBitmapToFile(appContext, output)

            // Make a Notification displaying the URI by calling the makeStatusNotification method from WorkerUtils.
            makeStatusNotification("Output is $outputUri", appContext)

            // Add output to result
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying blur")
            throwable.printStackTrace()
            Result.failure()
        }
    }
}