package com.nbt.blytics

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp

/**
 * Created bynbton 29-06-2021
 */
class AppMain : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_PAUSE) {
                    Log.e("Lifecycle", "Background");
                    IS_APP_IN_BACKGROUD = true
                    IS_COMING_FROM_SPLASH = false
                } else if (event == Lifecycle.Event.ON_RESUME) {
                    Log.e("Lifecycle", "Foreground");
                    IS_APP_IN_BACKGROUD = false
                }
            }
        })
         IS_APP_IN_BACKGROUD=true
    }
    companion object{
        var IS_APP_IN_BACKGROUD=false
        var IS_COMING_FROM_SPLASH = true
    }
}