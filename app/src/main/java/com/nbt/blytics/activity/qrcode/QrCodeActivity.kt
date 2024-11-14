package com.nbt.blytics.activity.qrcode

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.nbt.blytics.BuildConfig
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.ActivityQrCodeBinding
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.SharePreferences.USER_PROFILE_IMAGE
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset


class QrCodeActivity : BaseActivity() {
    private lateinit var mBinding: ActivityQrCodeBinding
    private var qrUrl:String =""
    private val qrCodeViewModel: QrCodeViewModel by viewModels()
    private val CAMERA_PERMISSION = arrayOf(
        Manifest.permission.CAMERA
    )

    // private val hideHandler = Handler()
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*   window.setFlags(
               WindowManager.LayoutParams.FLAG_FULLSCREEN,
               WindowManager.LayoutParams.FLAG_FULLSCREEN
           )*/
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_qr_code)
        mBinding.activityQrImage.setFormats(listOf(BarcodeFormat.QR_CODE))
        mBinding.activityQrImage.setAutoFocus(true)

        setSupportActionBar(mBinding.toolbarTop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Qr pay"
        val showQR = intent.getStringExtra(Constants.SHOW_QR)
        observer()
        mBinding.qrHolder.hide()
        val userProfile = pref.getStringValue(USER_PROFILE_IMAGE, "")
        Glide.with(this)
            .load(userProfile)
            .error(R.drawable.dummy_user)
            .into(mBinding.userImage)

        if (showQR.equals(Constants.AnsYN.YES.name, true)) {
            mBinding.apply {
                homeQrText.hide()
                btnCancelScan.hide()
                btnScan.hide()
                lytUserInfo.show()
                home.hide()
                cvShowQr.show()
                btnClose.show()


                homeQrText.text = "QR Code"
                userName.text = pref.getStringValue(SharePreferences.USER_FIRST_NAME, "") + " " +
                        pref.getStringValue(SharePreferences.USER_LAST_NAME, "")
            }


            showLoading()
            qrCodeViewModel.getQrCode(
                QrRequest(
                    pref.getStringValue(SharePreferences.USER_ID, ""),
                    pref.getStringValue(SharePreferences.USER_TOKEN, "")
                )
            )
        } else {

            if (checkPermission()) {
                mBinding.activityQrImage.startCamera()
                mBinding.home.visibility = View.INVISIBLE
                mBinding.activityQrImage.visibility = View.VISIBLE
                mBinding.btnScan.visibility = View.GONE
                mBinding.btnCancelScan.visibility = View.VISIBLE
               // mBinding.homeQrText.text = "Scan QR Code"
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(CAMERA_PERMISSION, 0)
                }
            }
        }
        mBinding.btnClose.setOnClickListener {
            finish()
        }


        mBinding.activityQrImage.setResultHandler(ZXingScannerView.ResultHandler {
            if (it != null) {
                Log.d("SCAN_QR_1", it.text)
                val data = android.util.Base64.decode(it.text, android.util.Base64.DEFAULT)
                val decryptData = String(data, Charset.forName("UTF-8"))
                try {


                    val receiverInfo =
                        Gson().fromJson<ReceiverInfo>(decryptData, ReceiverInfo::class.java)

                    val intent = Intent(this, BconfigActivity::class.java)
                    intent.apply {
                        putExtra(PayAmountFragment.IS_MULTI_PAY, "no")
                        putExtra(PayAmountFragment.RECEIVER_NAME, receiverInfo.user_name)
                        putExtra(PayAmountFragment.RECEIVER_CONTACT, receiverInfo.contact_no)
                        putExtra(PayAmountFragment.RECEIVER_IMG, receiverInfo.avatar_url)
                        putExtra(Constants.PAY_MODE, Constants.PayType.SENT_MONEY.name)
                    }
                    intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_AMOUNT.name)
                    startActivity(intent)
                    finish()
                } catch (ex: Exception) {
                    showToast("Invalid QR")
                }
            }
        })
        happyThemeChanges()



       // mBinding.homeQrText.text = "Scan QR Code"
        mBinding.btnScan.setOnClickListener {
            if (checkPermission()) {
                mBinding.activityQrImage.startCamera()
                mBinding.home.visibility = View.INVISIBLE
                mBinding.activityQrImage.visibility = View.VISIBLE
                mBinding.btnScan.visibility = View.GONE
                mBinding.btnCancelScan.visibility = View.VISIBLE
               // mBinding.homeQrText.text = "Scan QR Code"
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(CAMERA_PERMISSION, 0)
                }
            }
        }
        mBinding.btnCancelScan.setOnClickListener {
            mBinding.home.visibility = View.VISIBLE
            mBinding.activityQrImage.visibility = View.GONE
            mBinding.btnScan.visibility = View.VISIBLE
            mBinding.btnCancelScan.visibility = View.GONE
        }

        mBinding.btnShareQr.setOnClickListener {
            shareImage(qrUrl, "Qr Code")
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            0 -> {
                if (grantResults.size > 0) {
                    val cameraAccept =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccept) {
                        /* Toast.makeText(requireContext(), "permission granted", Toast.LENGTH_LONG)
                             .show()*/
                        mBinding.activityQrImage.startCamera()
                        mBinding.home.visibility = View.GONE
                        mBinding.activityQrImage.visibility = View.VISIBLE
                        mBinding.btnScan.visibility = View.GONE
                        mBinding.btnCancelScan.visibility = View.VISIBLE


                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CAMERA
                                )
                            ) {
                                ActivityCompat.requestPermissions(
                                    this, CAMERA_PERMISSION, 0
                                )

                            } else {
                                AlertDialog.Builder(this)
                                    .setTitle("need camera permission")
                                    .setMessage("need permission to scan code")
                                    .setPositiveButton(
                                        "OK",
                                        DialogInterface.OnClickListener { dialogInterface, i ->
                                            val intent = Intent()
                                            intent.action =
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                            val uri: Uri =
                                                Uri.fromParts(
                                                    "package",
                                                    packageName,
                                                    null
                                                )
                                            intent.data = uri
                                            startActivity(intent)
                                        })
                                    .create()
                                    .show()

                            }
                        }

                    }
                }
            }
        }
    }


    private fun happyThemeChanges() {

        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mBinding.toolbarTop.setBackgroundResource(R.color.black)
                mBinding.activityQr.setBackgroundResource(R.color.b_bg_color_dark)
                mBinding.btnScan.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                mBinding.btnCancelScan.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                mBinding.btnClose.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                mBinding.cvShowQr.setCardBackgroundColor(resources.getColor(R.color.black))
                mBinding.cvShowQr2.setCardBackgroundColor(resources.getColor(R.color.black))
                mBinding.home.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mBinding.toolbarTop.setBackgroundResource(R.color.white)
                mBinding.activityQr.setBackgroundResource(R.color.b_bg_color_light)
                mBinding.btnScan.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                mBinding.btnCancelScan.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                mBinding.btnClose.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                mBinding.cvShowQr.setCardBackgroundColor(resources.getColor(R.color.white))
                mBinding.cvShowQr2.setCardBackgroundColor(resources.getColor(R.color.white))
                mBinding.home.setColorFilter(
                    ContextCompat.getColor(this, R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                mBinding.toolbarTop.setBackgroundResource(R.color.white)
                mBinding.activityQr.setBackgroundResource(R.color.b_bg_color_light)
                mBinding.btnScan.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                mBinding.btnCancelScan.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                mBinding.btnClose.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                mBinding.cvShowQr.setCardBackgroundColor(resources.getColor(R.color.white))
                mBinding.cvShowQr2.setCardBackgroundColor(resources.getColor(R.color.white))
                mBinding.home.setColorFilter(
                    ContextCompat.getColor(this, R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

            }
        }

    }

    private fun observer() {
        qrCodeViewModel.observerResponse.observe(this) {
            when (it) {
                is QrResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        mBinding.home.hide()
                        mBinding.qrHolder.show()
                        qrUrl =it.data.qrcode
                        mBinding.qrHolder.setImage(it.data.qrcode)
                    }
                    qrCodeViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    qrCodeViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    hideLoading()
                    qrCodeViewModel.observerResponse.value = null
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.activityQrImage.stopCamera()
    }

    fun isJSONValid(test: String): Boolean {
        try {
            JSONObject(test)
        } catch (ex: JSONException) {
            return false
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun shareImage(url:String, title:String){
        Glide.with(this)
            .asBitmap()
            .override(500, 500)
            .load(url)
            .into( object: CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    bitmap: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "image/*"
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    i.putExtra(Intent.EXTRA_TEXT, mBinding.userName.text.toString().trim())
                    i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap))
                    startActivity(Intent.createChooser(i, title))

                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
    }
    fun getLocalBitmapUri(bmp: Bitmap): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".png"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            //Uri.fromFile(file)
            bmpUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }


}


data class ReceiverInfo(
    val user_name: String,
    val contact_no: String,
    val avatar_url: String = ""
)