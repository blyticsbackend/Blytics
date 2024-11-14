package com.nbt.blytics.testing

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.databinding.ActivityUserNew2Binding
import com.nbt.blytics.utils.Constants
import org.json.JSONArray
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.Executor
import kotlin.io.path.createTempDirectory
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


class UserNewActivity : BaseActivity() {

    lateinit var binding: ActivityUserNew2Binding
    private lateinit var executor: Executor
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_new2)




        val lo = mutableListOf<Location>()

        lo.sortBy{ it.distace }
        GsonBuilder().create().toJsonTree(lo).asJsonArray


        /* binding.btnNameEdt.setOnClickListener {
             if(binding.btnNameEdt.text.toString()=="Edit"){
                 binding.btnNameEdt.text ="Save"
             }else{
                 binding.btnNameEdt.text ="Edit"
             }
         }*/


        /* val intent = Intent(this, BconfigActivity::class.java)
         intent.putExtra(Constants.COMING_FOR, ComingFor.MANAGE_AC.name)
         startActivity(intent)*/
        /*     executor = ContextCompat.getMainExecutor(this)
             setPrompt()

             if (UtilityHelper.isBiometricHardWareAvailable(this)) {


                 initBiometricPrompt(
                     Constants.BIOMETRIC_AUTHENTICATION,
                     Constants.BIOMETRIC_AUTHENTICATION_SUBTITLE,
                     Constants.BIOMETRIC_AUTHENTICATION_DESCRIPTION,
                     false
                 )
             } else {


                 //Fallback, use device password/pin
                 if (UtilityHelper.deviceHasPasswordPinLock(this)) {

                     initBiometricPrompt(
                         Constants.PASSWORD_PIN_AUTHENTICATION,
                         Constants.PASSWORD_PIN_AUTHENTICATION_SUBTITLE,
                         Constants.PASSWORD_PIN_AUTHENTICATION_DESCRIPTION,
                         true
                     )
                 }*/


        //  biometricAuth()
    }


    private fun biometricAuth() {
        fun keyGuard() {
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i =
                    km.createConfirmDeviceCredentialIntent("Authentication required", "password")
                startActivityForResult(i, Constants.CODE_AUTHENTICATION_VERIFICATION)
            } else Toast.makeText(
                this,
                "No any security setup.",
                Toast.LENGTH_SHORT
            ).show()

        }

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    MainActivity.isBioAuth = false
                    /*  Toast.makeText(
                          applicationContext,
                          "Authentication error: $errString", Toast.LENGTH_SHORT
                      )
                          .show()*/
                    keyGuard()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    MainActivity.isBioAuth = true
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    MainActivity.isBioAuth = false
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Blytics app")
            .setSubtitle("Log in using biometric credential")
            .setNegativeButtonText("Use pattern lock")
            .build()


        val sheet = AuthBottomSheetDialog {
            biometricPrompt.authenticate(promptInfo)
        }
        sheet.isCancelable = false
        sheet.show(supportFragmentManager, "exampleBottomSheet")


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.CODE_AUTHENTICATION_VERIFICATION) {
            if (resultCode != RESULT_OK) {
                /*if(biometricPrompt!=null){
                    biometricPrompt?.cancelAuthentication()
                }*/

                // biometricAuth()
            } else {
                // biometricPrompt?.cancelAuthentication()
            }
        }


    }





}

fun main() {

}

fun distanceBTLocation(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val theta = lon1 - lon2
    var dist = (sin(deg2rad(lat1))
            * sin(deg2rad(lat2))
            + (cos(deg2rad(lat1))
            * cos(deg2rad(lat2))
            * cos(deg2rad(theta))))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515
    dist *= 1.60934
    return dist
}

private fun deg2rad(deg: Double): Double {
    return deg * Math.PI / 180.0
}

private fun rad2deg(rad: Double): Double {
    return rad * 180.0 / Math.PI
}

fun Double.roundOffDecimal(): Double? {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}


data class Location(
    val name: String,
    val distace: Double
)

