package com.nbt.blytics.firebasenotification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.utils.Constants.COMING_FROM_NOTIFICATION
import com.nbt.blytics.utils.Constants.NOTIFICATION_INFO_BUNDLE
import com.nbt.blytics.utils.Constants.NOTIFICATION_TYPE
import com.nbt.blytics.utils.Constants.PUSH_NOTIFICATION
import com.nbt.blytics.firebasenotification.ScheduledWorker.Companion.NOTIFICATION_MESSAGE

import com.nbt.blytics.firebasenotification.ScheduledWorker.Companion.NOTIFICATION_TITLE
import com.nbt.blytics.utils.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.random.Random


@ExperimentalCoroutinesApi
class NotificationUtil(private val context: Context) {



    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(title: String?, message: String?, imageUrl: Uri?) {
        if (title == null || message == null) {
            return
        }
        val pushNotification = Intent(PUSH_NOTIFICATION)

        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
        val intent = Intent(context, BconfigActivity::class.java)
      /*  val bundle = bundleOf(
            NOTIFICATION_TITLE to title,
            NOTIFICATION_MESSAGE to message, NOTIFICATION_TYPE to " "
        )*/

      //  intent.putExtra(NOTIFICATION_INFO_BUNDLE, bundle)
        intent.putExtra(Constants.COMING_FOR, ComingFor.NOTIFICATION_LIST.name)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =   if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_MUTABLE
            )
        }else{
            PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT
            )
        }



        var bitmap: Bitmap? = null
        if (imageUrl != null) {

            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        @Nullable transition: Transition<in Bitmap?>?
                    ) {
                        bitmap = resource

                    }

                    override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                })
        }

        val channelId = context.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            //  .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)


        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (bitmap != null && imageUrl != null) {
            notificationManager.notify(
                Random.nextInt(), notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                ).build()
            )
        } else {
            notificationManager.notify(Random.nextInt(), notificationBuilder.build())
        }
    }
}



