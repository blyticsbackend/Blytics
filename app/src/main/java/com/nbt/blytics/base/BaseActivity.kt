package com.nbt.blytics.base

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.nbt.blytics.R
import com.nbt.blytics.activity.SplashActivity
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.activity.main.LogoutRequest
import com.nbt.blytics.activity.main.LogoutResponse
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.callback.verificationLiveData
import com.nbt.blytics.database.BlyticsDatabase
import com.nbt.blytics.modules.home.HomeFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.SharePreferences.INITIAL_ACTIVE_TIME
import com.nbt.blytics.utils.SharePreferences.IS_TIMER_ACTIVATED
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created bynbton 11-06-2021.
 */

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: Dialog
    private lateinit var mProgressDialogTransparant: Dialog
    private lateinit var mAuth: FirebaseAuth
    private lateinit var baseViewModel: BaseViewModel
    lateinit var db: BlyticsDatabase

    lateinit var pref: SharePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        pref = SharePreferences.getInstance(this)
        db = BlyticsDatabase.getInstance(this)
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        NetworkUtils.getNetworkLiveData(this)
        if (isCustomMode) {
            setTheme(R.style.Theme_Blytics_Happy)
        }
        observer()
        //  switchToThemeMode(THEME.MODE_DEFAULT)


        //   toolbarViewModel = ViewModelProviders.of(this).get(ToolbarViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
       // currentTimeHandler( pref.getStringValue(INITIAL_ACTIVE_TIME,""))
    }


    /**
     * For show loader
     */
    fun showLoading() {
        try {
            if(!isDestroyed) {
                if (::mProgressDialog.isInitialized) {
                    if (!mProgressDialog.isShowing) {
                        mProgressDialog.show()
                    }
                } else {
                    mProgressDialog = UtilityHelper.showDialog(this)!!
                }
            }
        }catch (ex:Exception){

        }
    }

    /**
     * For hide loader
     */
    fun hideLoading() {
        if(!isDestroyed) {
        if (::mProgressDialog.isInitialized && mProgressDialog.isShowing) {
            try {
                mProgressDialog.dismiss()
            } catch (ex: Exception) {
                Log.d("Progress Bar error--", ex.message.toString())
            }
        }
        }
    }


    /**
     * For show loader
     */
    fun showLoadingTransperant() {
        try {
            if(!isDestroyed) {
                if (::mProgressDialogTransparant.isInitialized) {
                    if (!mProgressDialogTransparant.isShowing) {
                        mProgressDialogTransparant.show()
                    }
                } else {
                    mProgressDialogTransparant = UtilityHelper.showDialogTrasnperant(this)!!
                }
            }
        }catch (ex:Exception){

        }
    }

    /**
     * For hide loader
     */
    fun hideLoadingTransperant() {
        if(!isDestroyed) {
            if (::mProgressDialogTransparant.isInitialized && mProgressDialogTransparant.isShowing) {
                try {
                    mProgressDialogTransparant.dismiss()
                } catch (ex: Exception) {
                    Log.d("Progress Bar error--", ex.message.toString())
                }
            }
        }
    }

    fun showToast(message: String, lenght: Int = Toast.LENGTH_SHORT) {
        if(!isDestroyed) {
            Toast.makeText(this, message, lenght).show()
        }
    }

    fun showSnack(view: View, message: String, lenght: Int = Snackbar.LENGTH_SHORT) {
        if(!isDestroyed) {
            Snackbar.make(view, message, lenght).show()
        }
    }

    companion object {
        var isCustomMode = false
    }


    fun showThemeDialog() {
        val items = arrayOf("Light", "Dark")

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Theme")
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> {
                        switchToThemeMode(THEME.MODE_DEFAULT)
                        pref.setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                        pref.setIntValue(SharePreferences.SELECTED_MODE, 0)
                    }
                    1 -> {
                        switchToThemeMode(THEME.MODE_NIGHT)
                        pref.setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                        pref.setIntValue(SharePreferences.SELECTED_MODE, 1)
                    }

                }
            }
            .show()
    }


    data class CodeSentModel(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken
    )

    data class VerificationCompleteModel(
        val fieldBaseUser: FirebaseUser?
    )

    data class FirebaseExceptionModel(
        val e: Exception
    )

    data class CodeAutoRetrievalTimeOutModel(
        val s0: String
    )

    fun sendVerificationCode(phoneNumber: String) {

        showLoading()

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setActivity(this)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbackFirebase)
                .build()
        )

    }

    private val callbackFirebase =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                hideLoading()
                verificationLiveData.value = CodeSentModel(verificationId, token)
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                hideLoading()
                Log.d("TAG", "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)

            }

            override fun onVerificationFailed(e: FirebaseException) {
                hideLoading()
                verificationLiveData.value = FirebaseExceptionModel(e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // showToast(e.message.toString())
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showToast(e.message.toString())
                }

            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                hideLoading()
                verificationLiveData.value = CodeAutoRetrievalTimeOutModel(p0)
            }

        }

    fun verifyPhoneNumberWithCode(
        verificationId: String?,
        code: String?
    ) {
        val credential = PhoneAuthProvider.getCredential(
            verificationId!!,
            code!!
        )
        signInWithPhoneAuthCredential(credential)
    }


    private fun logoutSession(){
        if( pref.getStringValue(SharePreferences.USER_ID, "").isNotBlank()) {
            showLoading()
            baseViewModel.logOutSession(
                LogoutRequest(
                    pref.getStringValue(SharePreferences.USER_ID, ""),
                    pref.getStringValue(SharePreferences.USER_TOKEN, ""),
                )
            )
        }
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential
    ) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    hideLoading()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")

                    val user = task.result?.user
                    verificationLiveData.value = VerificationCompleteModel(user)
                    mAuth.signOut()
                } else {
                    hideLoading()
                    // Sign in failed, display a message and update the UI
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        //   showToast("The verification code entered was invalid")
                    }
                    verificationLiveData.value = FirebaseExceptionModel(task.exception!!)
                }
            }
    }

    fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        showLoading()
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setActivity(this)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbackFirebase)
                .setForceResendingToken(token)
                .build()
        )
    }


    override fun onStop() {
        super.onStop()
   /*     pref.setBooleanValue(SharePreferences.IS_TIMER_ACTIVATED,true )
        saveCurrentTime()*/
    }
    override fun onDestroy() {
        super.onDestroy()



    }
    private fun saveCurrentTime(){
        val initialActiveTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(Date())
        pref.setStringValue(INITIAL_ACTIVE_TIME, initialActiveTime)

    }

    private fun currentTimeHandler(initActiveTime: String) {
        if (pref.getBooleanValue(IS_TIMER_ACTIVATED)) {
                    val currentFormatDate =
                        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(Date())
                    val currentTime =
                        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).parse(currentFormatDate)
                    val lastDate =
                        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).parse(initActiveTime)


                    val differenceInTime = currentTime.time - lastDate.time
                    val differenceInMinutes: Long = ((differenceInTime
                            / (1000 * 60))
                            % 60)
                    Log.d("Timer...",differenceInMinutes.toString())
                    if (differenceInMinutes >= 10) {
                        pref.setBooleanValue(IS_TIMER_ACTIVATED, false)
                        if(pref.getStringValue(SharePreferences.USER_ID,"").isNotBlank()) {
                            logoutSession()

                        }



                    }
                }

            //handler.postDelayed(runnable, 10)
        }

    private fun observer() {
        baseViewModel.baseObserverResponse.observe(this) {
            when (it) {
                is LogoutResponse -> {
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        logout()
                        finishAffinity()
                        val intent = Intent(this@BaseActivity, SplashActivity::class.java)
                        startActivity(intent)
                    } else {
                        showToast(it.message)
                    }
                    hideLoading()
                    baseViewModel.baseObserverResponse.value = null

                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    baseViewModel.baseObserverResponse.value = null

                }
                is Throwable ->{
                    hideLoading()
                    baseViewModel.baseObserverResponse.value = null
                }
            }
        }
    }

    private fun logout() {
        pref.apply {
            HomeFragment.IS_SWITCH_SHOWN = false
            val userMobileNumber =
                pref.getStringValue(USER_MOBILE_NUMBER, "")
            val userProfile = pref.getStringValue(USER_PROFILE_IMAGE, "")
            val userName = pref.getStringValue(USER_FIRST_NAME, "")
            val deviceToken = pref.getStringValue(DEVICE_TOKEN, "")
            pref.clearPreference()
            pref.setStringValue(pref.USER_MOBILE_NUMBER, userMobileNumber)
            pref.setStringValue(pref.USER_PROFILE_IMAGE, userProfile)
            pref.setStringValue(pref.USER_FIRST_NAME, userName)
            pref.setStringValue(pref.DEVICE_TOKEN, deviceToken)
            //setDefaultData()
            finishAffinity()
            startActivity(Intent(this@BaseActivity, UserActivity::class.java))
        }
    }


}
/*
    fun showsnack(view: View, message: String): Snackbar {
        val snackbar = Snackbar
            .make(view, message, Snackbar.LENGTH_LONG)
        snackbar.show()
        return snackbar
    }
}*/
