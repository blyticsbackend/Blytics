package com.nbt.blytics.firebasenotification

import android.net.Uri
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nbt.blytics.callback.notificationCallBack
import com.nbt.blytics.utils.SharePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FMService : FirebaseMessagingService() {
    var imageUrl: Uri? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage}")
            // Get Message details
                val title = remoteMessage.data["title"]
            val message = remoteMessage.data["message"]
            if (remoteMessage.data.contains("restaurent_image")) {
                imageUrl = Uri.parse(remoteMessage.data.get("restaurent_image"))

            } else {
                imageUrl = null

            }
            Log.d(TAG, "Message data payload:==========${remoteMessage.notification?.title}")
            // Check that 'Automatic Date and Time' settings are turned ON.
            // If it's not turned on, Return
            if (!isTimeAutomatic(applicationContext)) {
                Log.d(TAG, "`Automatic Date and Time` is not enabled")
                return
            }


            showNotification(title, message, imageUrl)
            notifyNotification()

        }
    }

  private  fun notifyNotification(){
      GlobalScope.launch {
          withContext(Dispatchers.Main) {
              notificationCallBack.value = true

          }
      }
    }



    private fun showNotification(title: String?, message: String?,imageUrl:Uri?) {

        NotificationUtil(applicationContext).showNotification(title, message,imageUrl)

    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        SharePreferences.getInstance(applicationContext).setStringValue(SharePreferences.DEVICE_TOKEN, token)
    }

    companion object {
        private const val TAG = "FMService"
    }
}