package com.nbt.blytics.activity

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.callback.verificationLiveData
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.ActivityUserBinding
import com.nbt.blytics.utils.*
import java.util.concurrent.TimeUnit


class UserActivity : BaseActivity() {
    lateinit var mBinding: ActivityUserBinding
  //  lateinit var mAuth: FirebaseAuth
    private var phoneNumber: String = ""
    lateinit var navController: NavController
    var deviceModel = ""
    var version = ""
    var apiLevel = ""
    var fingerPrint = ""
    var deviceId = ""
    private lateinit var dialogNetworkBuilder: MaterialAlertDialogBuilder
    private var networkDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //showSystemUI()
        /*window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN*/
       // mAuth = FirebaseAuth.getInstance()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user)
        setSupportActionBar(mBinding.toolbarTop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        dialogNetworkBuilder = MaterialAlertDialogBuilder(this)
        navController = findNavController(R.id.phone_login_frag_container)
        networkConnectionObserver()
        happyThemeChanges()
        getDeviceInfo()
        setupWithBottomNavView()
     /*   mBinding.lblLogin.isEnabled = false

        mBinding.lblLogin.setOnClickListener {
            val currentFragment = getCurrentFragment()!!::class.java.simpleName
            if (currentFragment.equals("PhoneRegistrationFragment", true)) {
                navController.navigate(R.id.action_phoneRegistrationFragment_to_signInFragment)
            } else {
                navController.popBackStack(R.id.signInFragment, true)
                navController.navigate(R.id.signInFragment)
            }
        }*/

    /*    mBinding.lblSignUp.setOnClickListener {
            val currentFragment = getCurrentFragment()!!::class.java.simpleName
            if (currentFragment.equals("SignInFragment", true)) {
                navController.navigate(R.id.action_signInFragment_to_phoneRegistrationFragment, bundleOf(Constants.COMING_FOR to ComingFor.SIGN_UP.name))
            } else {
                navController.popBackStack(R.id.phoneRegistrationFragment, true)
                navController.navigate(R.id.phoneRegistrationFragment)
            }
        }*/
        observerDestination()
    }

    fun toolbarTitle(title:String){
        mBinding.toolbarTop.title = title
    }

    fun toolbarHide(){
        mBinding.toolbarTop.hide()
    }
    fun toolbarShow(){
        mBinding.toolbarTop.show()
    }

    private fun happyThemeChanges() {
        if (isCustomMode) { }
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mBinding.toolbarTop.setTitleTextColor(resources.getColor(R.color.white))
                mBinding.toolbarTop.textAlignment= View.TEXT_ALIGNMENT_CENTER
                mBinding.toolbarTop.setBackgroundResource(R.color.b_bg_color_dark)
                //mBinding.topLyt.setBackgroundResource(R.color.night_black)
                /*   mBinding.lblLogin.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
                   mBinding.lblSignUp.setTextColor(ContextCompat.getColor(this, R.color.white))*/
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mBinding.toolbarTop.setTitleTextColor(resources.getColor(R.color.b_currency_blue))
                mBinding.toolbarTop.textAlignment= View.TEXT_ALIGNMENT_CENTER
                mBinding.toolbarTop.setBackgroundResource(R.color.b_bg_color_light)
                //mBinding.topLyt.setBackgroundResource(R.color.white)
                /*  mBinding.lblLogin.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
                  mBinding.lblSignUp.setTextColor(ContextCompat.getColor(this, R.color.black))
  */            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                mBinding.toolbarTop.setTitleTextColor(resources.getColor(R.color.b_currency_blue ))
                mBinding.toolbarTop.textAlignment= View.TEXT_ALIGNMENT_CENTER
                mBinding.toolbarTop.setBackgroundResource(R.color.b_bg_color_light)
                //   mBinding.topLyt.setBackgroundResource(R.color.white)
                /*      mBinding.lblLogin.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
                      mBinding.lblSignUp.setTextColor(ContextCompat.getColor(this, R.color.black))
                 */ }
        }
    }

    private fun getDeviceInfo() {
        DeviceInfoHelper(this).apply {
            Log.d("********", "*****************")
            Log.d("deviceModel", this.model)
            Log.d("version", version.toString())
            Log.d("apiLevel", apiLevel.toString())
            Log.d("fingerPrint", fingerPrint.toString())
            Log.d("id", id.toString())
            deviceModel = model
            this@UserActivity.version = version.toString()
            this@UserActivity.apiLevel = apiLevel.toString()
            this@UserActivity.fingerPrint = fingerPrint.toString()
            this@UserActivity.deviceId = id.toString()
            Log.d("imei", this.imei.toString())
            Log.d("hardware", this.hardware.toString())
            Log.d("board", this.board.toString())
            Log.d("bootloader", bootloader.toString())
            Log.d("user", user.toString())
            Log.d("host", host.toString())
            Log.d("time", time.toString())
            Log.d("display", display.toString())
            Log.d("-----", "*****************")
        }
    }

    private fun networkConnectionObserver() {
        NetworkUtils.networkLiveData.observe(this) {
            when (it) {
                true -> {
                    networkDialog?.apply {
                        if (isShowing) {
                            dismiss()
                            networkDialog = null
                        }
                    }
                    NetworkUtils.networkLiveData.value = null
                }
                false -> {
                    showDialogNetwork()
                    NetworkUtils.networkLiveData.value = null
                }
            }
        }
    }

    private fun showDialogNetwork() {
        dialogNetworkBuilder.apply {
            setTitle("No Internet.")
            setMessage("Please check internet connectivity.")
            setCancelable(false)
        }
        if (networkDialog == null)
            networkDialog = dialogNetworkBuilder.show()
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
      // if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun observerDestination() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            toolbarShow()
            toolbarTitle("")
            when(destination.id){
                R.id.signInFragment ->{
                    toolbarHide()
                }
                R.id.phoneRegistrationFragment ->{
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                onBackPressed()
                return true
            }
            else->{
                return super.onOptionsItemSelected(item)
            }
        }
    }
    private fun getCurrentFragment(): Fragment? {
        val navHost = supportFragmentManager.findFragmentById(R.id.phone_login_frag_container)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                when(fragment.id){
                    R.id.signInFragment ->{
                        toolbarHide()
                    }
                }
                return fragment
            }
        }
        return null
    }

    private fun setupWithBottomNavView() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.signInFragment -> {
                 //   mBinding.topLyt.setBackgroundResource(R.color.white)
                   /* mBinding.lblLogin.isEnabled = false
                    mBinding.lblSignUp.isEnabled = true*/
                    when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                          //  mBinding.topLyt.setBackgroundResource(R.color.night_black)
                          /*  mBinding.lblLogin.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.gray_light
                                )
                            )*/
                       /*     mBinding.lblSignUp.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.white
                                )
                            )*/
                        }
                        Configuration.UI_MODE_NIGHT_NO -> {
                           // mBinding.topLyt.setBackgroundResource(R.color.white)
                        /*    mBinding.lblLogin.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.gray_dark
                                )
                            )*/
                           /* mBinding.lblSignUp.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.black
                                )*/
                            //)
                        }
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                           // mBinding.topLyt.setBackgroundResource(R.color.white)
                          /*  mBinding.lblLogin.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.gray_dark
                                )
                            )*/
                      /*      mBinding.lblSignUp.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.black
                                )
                            )*/
                        }
                    }
                }

                R.id.phoneRegistrationFragment -> {
                    when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                           // mBinding.topLyt.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                        }
                        Configuration.UI_MODE_NIGHT_NO -> {
                            //mBinding.topLyt.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)
                        }
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                           // mBinding.topLyt.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)
                        }
                    }
                    /*mBinding.lblLogin.isEnabled = true
                    mBinding.lblSignUp.isEnabled = false*/
                 /*   mBinding.lblSignUp.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.gray_light
                        )
                    )*/
                   // mBinding.lblLogin.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                else -> {
                }
            }
        }
    }
}