package com.nbt.blytics.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created bynbton 11-06-2021.
 */
object UtilityHelper {

    private val suffixes = TreeMap<Long, String>()
    val suffixess = TreeMap<Long, String>()


    init {
        suffixes[1_000L] = "K"
        suffixes[1_000_000L] = "M"
        suffixes[1_000_000_000L] = "B"
        suffixes[1_000_000_000_000L] = "T"
        suffixes[1_000_000_000_000_000L] = "P"
        suffixes[1_000_000_000_000_000_000L] = "E"



        suffixess.put(1_000L, "K")
        suffixess.put(1_000_000L, "M")
        suffixess.put(1_000_000_000L, "B")
        suffixess.put(1_000_000_000_000L, "T")
        suffixess.put(1_000_000_000_000_000L, "P")
        suffixess.put(1_000_000_000_000_000_000L, "E")
    }

    /* fun formatCurrency(value: Long): String {
         //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
         if (value == java.lang.Long.MIN_VALUE) return formatCurrency(java.lang.Long.MIN_VALUE + 1)
         if (value < 0) return "-" + formatCurrency(-value)
         if (value < 9001) return java.lang.Long.toString(value) //deal with easy case

         val daa = 5900343L // ->
         val e = suffixes.floorEntry(value)
         val divideBy = e.key
         val suffix = e.value
         var truncated = 0L
         var total = value.toString()
         try {
             truncated = value / (divideBy!!) //the number part of the output times 10
             val afterDecimal = value % divideBy
             total = if (afterDecimal > 0) {
                 val s = String.format("%2d", afterDecimal)
                 val ne = afterDecimal.toString().substring(0, afterDecimal.toString().count() / 2)
                 truncated.toString() + "." + ne + suffix
             } else {
                 truncated.toString() + suffix
             }
             return total

         } catch (e: Exception) {
             e.printStackTrace()
         }
         return total
     }*/

    ///////////////////////////////////////////


    fun formatCurrency(value: Long): String {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatCurrency(Long.MIN_VALUE + 1)
        if (value < 0) return "-" + formatCurrency(-value)
        if (value < 9001) return value.toString() //deal with easy case

        val e = suffixess.floorEntry(value)
        val divideBy = e.key
        val suffix = e.value

        val truncated = value / (divideBy / 10) //the number part of the output times 10
        val hasDecimal =
            truncated < 100 && (truncated / 10.0).toString() != (truncated / 10).toString()
        return if (hasDecimal) ((truncated / 10).toString() + suffix) else (truncated / 10).toString() + suffix
    }


    fun formatValue(value: Long): String {
        var values = value
        val arr = arrayOf("", "K", "M", "B", "T", "P", "E")
        var index = 0
        while (values / 1000 >= 1) {
            values /= 1000
            index++
        }
        val decimalFormat = DecimalFormat("#.##")
        return String.format("%s %s", decimalFormat.format(values), arr[index])
    }

    fun convertToTime(time: Long): String {
        val stringTime = StringBuilder()

        stringTime.append(
            String.format(
                Locale.ENGLISH,
                "%2d",
                Math.round((time / (1007 * 60)).toFloat())
            )
        )
        stringTime.append(":")

        val seconds = Math.round((time % (1007 * 60) / 1000).toFloat())
        stringTime.append(String.format(Locale.ENGLISH, if (seconds > 9) "%2d" else "0%d", seconds))

        return stringTime.toString()
    }

    /**
     * Connection Available or not
     */
    fun isConnectionAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ))
        }
        return false
    }


    fun openPdf(activity: Activity, url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(browserIntent)
    }


    /**
     * To hide the soft key pad if open
     */
    fun hideSoftKeypad(context: Context) {
        val activity = context as Activity
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            activity.currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    /**
     * To show the soft key pad
     */

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /**
     * To show dialog
     */
    fun showDialog(context: Context?): Dialog? {
        try {
            if(context ==null){
                return null
            }
            val progressDialog = Dialog(context!!)
            if (progressDialog.window != null) {
                progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            progressDialog.show()
            progressDialog.setContentView(R.layout.progress_dialog)
            progressDialog.setCancelable(false)
            progressDialog.setCanceledOnTouchOutside(false)

            return progressDialog
        }catch (ex:Exception){
            return null
        }


    }

    fun showDialogTrasnperant(context: Context?): Dialog? {
        try {
            if(context ==null){
                return null
            }
            val progressDialog = Dialog(context!!)
            if (progressDialog.window != null) {
                progressDialog.window!!.setBackgroundDrawable(ColorDrawable(context.resources.getColor(R.color.white_transparent)))
                progressDialog.window!!.setDimAmount(0f)
            }
            progressDialog.show()
            progressDialog.setContentView(R.layout.progress_dialog)
            progressDialog.findViewById<ImageView>(R.id.pb_img).hide()
            progressDialog.findViewById<ProgressBar>(R.id.pb_loading).hide()
            progressDialog.setCancelable(false)
            progressDialog.setCanceledOnTouchOutside(false)

            return progressDialog
        }catch (ex:Exception){
            return null
        }


    }

    fun getDeviceWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getDeviceHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }


    /**
     * To string to HTML
     */
    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }


    fun txnDateFormat(date: String): String {
        val data = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH).parse(date)
        return SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH).format(data)
    }
    fun txnTimeFormat(date: String): String {
        val data = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH).parse(date)
        return SimpleDateFormat("HH:mm", Locale.ENGLISH).format(data)
    }
    fun getFullMonthDate(dateFromServer: String): String {
        val pattern = "MMMM dd, yyyy"
        val inputPattern = "yyyy-MM-dd hh:mm:ss"
//        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        val df_input = SimpleDateFormat(inputPattern, Locale.getDefault())
        val df_output = SimpleDateFormat(pattern, Locale.getDefault())
        var outputDate = ""
        try {
            val parsed = df_input.parse(dateFromServer)
            outputDate = df_output.format(parsed)
        } catch (e: Exception) {
            Log.e("formattedDateFromString", "Exception in formateDateFromstring(): " + e.message)
        }

        return outputDate
    }

    /**
     * To set Image on Image View
     */

    fun navigateToActivity(
        callerActivity: BaseActivity,
        targetActivity: Class<out Activity>,
        dataBundle: Bundle?,
        isToFinishActivity: Boolean
    ) {
        val intent = Intent(callerActivity.applicationContext, targetActivity)
        dataBundle?.let { intent.putExtras(it) }
        callerActivity.startActivity(intent)
        if (isToFinishActivity) {
            callerActivity.finish()
        }

    }

/*    fun vibratePhone(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(20)
        }
    }*/

    infix fun Int.fdiv(i: Int): Double = this / i.toDouble()


    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    // Method to save an bitmap to a file
    fun bitmapToFile(bitmap: Bitmap, context: Context): File? {
        // Get the context wrapper
        val wrapper = ContextWrapper(context)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        // Return the saved bitmap uri
        return file
    }


    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this)
            .matches()
    }

    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }



    fun isBiometricHardWareAvailable(con: Context): Boolean {
        var result = false
        val biometricManager = BiometricManager.from(con)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> result = true
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> result = false
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> result = false
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> result = false
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> result = true
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> result = true
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> result = false
            }
        } else {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> result = true
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> result = false
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> result = false
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> result = false
            }
        }
        return result
    }



    fun deviceHasPasswordPinLock(con: Context): Boolean {
        val keymgr = con.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
        if (keymgr.isKeyguardSecure)
            return true
        return false
    }

    fun String.isDecimal():Boolean{
        val number = this.replace(",","")
        try {
            number.toDouble()
        }catch (ex:Exception){
            return false
        }
        return true
    }
}


