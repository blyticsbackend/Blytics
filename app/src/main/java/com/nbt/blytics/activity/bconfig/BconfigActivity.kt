package com.nbt.blytics.activity.bconfig

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.nbt.blytics.AppMain
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.activity.main.UserBlockRequest
import com.nbt.blytics.activity.main.UserBlockResponse
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.callback.profileUpdatedLiveData
import com.nbt.blytics.callback.valueChangeLiveData
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.ActivityBconfigBinding
import com.nbt.blytics.modules.home.HomeFragment
import com.nbt.blytics.modules.payment.models.UserProfileInfoResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.testing.AuthBottomSheetDialog
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show


class BconfigActivity : BaseActivity() {
    private lateinit var binding: ActivityBconfigBinding
    private lateinit var navController: NavController
    private var  biometricPrompt : BiometricPrompt? =null
    private lateinit var navHostFragment: NavHostFragment
    private  var authSheet : AuthBottomSheetDialog? =null
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val bconfigViewModel: BconfigViewModel by viewModels()
    var securityQuesList = mutableListOf<UserProfileInfoResponse.Data.SQ>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bconfig)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.bconfig_fragments_container) as NavHostFragment
        navController = navHostFragment.navController
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.bconfig)

        setSupportActionBar(binding.toolbarTop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        happyThemeChanges()
        if (intent.getStringExtra(Constants.COMING_FOR).equals(ComingFor.SQ.name)) {
            graph.startDestination = R.id.updateSQFragment
            supportActionBar?.title = "Security Question"
        } else if (intent.getStringExtra(Constants.COMING_FOR).equals(ComingFor.CHANGE_TPIN.name)) {
            graph.startDestination = R.id.changeTipFragment
            supportActionBar?.title = ""
        } else if (intent.getStringExtra(Constants.COMING_FOR).equals(ComingFor.CREATE_AC.name)) {
            supportActionBar?.title = "Add New account"
            graph.startDestination = R.id.addAccountFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR).equals(ComingFor.ALL_AC.name)) {
            supportActionBar?.title = ""
            graph.startDestination = R.id.allAccountFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR).equals(ComingFor.PROFILE.name)) {
            supportActionBar?.title = "Profile"
            graph.startDestination = R.id.newProfileFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR).equals(ComingFor.APPS.name)) {
            supportActionBar?.title = "App setting"
            graph.startDestination = R.id.appsFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.SECURITY_QUES.name)
        ) {
            supportActionBar?.title = "Security Question"
            graph.startDestination = R.id.securityQuestionFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.TRANSACTION_DETAILS.name)
        ) {
            supportActionBar?.title = "Details"
            graph.startDestination = R.id.singleTransactionFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.INFO.name)
        ) {
            supportActionBar?.title = "Info"
            graph.startDestination = R.id.infoFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.CONTACT.name)
        ) {
            supportActionBar?.title = "Contact"
            graph.startDestination = R.id.contactFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.TRANSACTION_HISTORY.name)
        ) {
            supportActionBar?.title = "Transactions"
            graph.startDestination = R.id.transactionHistoryFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.PAY_AMOUNT.name)
        ) {
            supportActionBar?.title = ""
            graph.startDestination = R.id.payAmountFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.PAY_NOW.name)
        ) {

            val mode = intent.getStringExtra(Constants.PAY_MODE)
            if (mode!!.equals(Constants.PayType.SENT_MONEY.name, true)) {
                supportActionBar?.title = "Send money"
            } else {
                supportActionBar?.title = "Request money"
            }
            if(mode.equals(Constants.PayType.RECENT_PAYEE.name, true)){
                supportActionBar?.title = "Recent payees"
            }
            if(mode.equals(Constants.PayType.SCHEDULE.name, true)){
                supportActionBar?.title ="Select user"
            }
            graph.startDestination = R.id.payNowFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.AC_TO_BANK.name)
        ) {
            supportActionBar?.title = "Pay a new non B account"
            graph.startDestination = R.id.actoBankFragment
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.LINK_AC_PHONE_VERIFY.name)
        ) {
            supportActionBar?.title = ""
            graph.startDestination = R.id.phoneRegistrationFragment3
        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.SELF_TRANSACTION.name)
        ) {
            supportActionBar?.title = "Transfer between account"
            graph.startDestination = R.id.selfTransferFragment
        }else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.NOTIFICATION_LIST.name)
        ) {
            supportActionBar?.title = "Notification"
            graph.startDestination = R.id.notificationFragment

        }else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.MANAGE_AC.name)
        ) {
            supportActionBar?.title = "Configure"
            graph.startDestination = R.id.manageFragment

        }
        else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.ACCOUNT_DETAILS.name)
        ) {
            supportActionBar?.title = "Account Details"
            graph.startDestination = R.id.allAcInfoFragment
        }
        else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.SET_SCHEDULE.name)
        ) {
            supportActionBar?.title = "Schedule date"
            graph.startDestination = R.id.setScheduleFragment

        }
        else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.PAYEE_HOME.name)
        ) {
            supportActionBar?.title = "Payee"
            graph.startDestination = R.id.payeeFragment

        }
        else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.TRANSACTION_HOME.name)
        ) {
            supportActionBar?.title = "Transaction"
            graph.startDestination = R.id.homeFragment

        }
        else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.SETTING_HOME.name)
        ) {
            supportActionBar?.title = "Setting"
            graph.startDestination = R.id.BAccountFragment

        }
        else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.CARD_HOME.name)
        ) {
            supportActionBar?.title = "Card"
            graph.startDestination = R.id.cardFragment

        }
        else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.LONE_HOME.name)
        ) {
            supportActionBar?.title = "Loan"
            graph.startDestination = R.id.moneyFragment

        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.CREATE_SCHEDULE.name)
        ) {
            supportActionBar?.title = "Create schedule payment"
            graph.startDestination = R.id.scheduleCreateFragment

        } else if (intent.getStringExtra(Constants.COMING_FOR)
                .equals(ComingFor.MANAGE_LINK_AC.name)
        ) {
            supportActionBar?.title = ""
            graph.startDestination = R.id.manageLinkedFragment

        }


        val navController = navHostFragment.navController
        navController.setGraph(graph, intent.extras)
        observerDestination()
        observer()


    }

    fun setToolbarTitle(title: String) {
        binding.toolbarTop.title = title
    }


    fun getProfileInfo() {
        pref.apply {
            showLoading()
            bconfigViewModel.getUserProfileInfo(
                getStringValue(USER_ID, ""),
                getStringValue(USER_TOKEN, "")
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if(AppMain.IS_APP_IN_BACKGROUD  ||AppMain.IS_COMING_FROM_SPLASH) {
            /* Handler().postDelayed({*/
            try {
                if (pref.getBooleanValue(SharePreferences.SYSTEM_LOCK_IS_ACTIVE)) {
                    biometricAuth()

                    if (authSheet == null) {
                        authSheet = AuthBottomSheetDialog {
                            biometricPrompt?.authenticate(promptInfo)
                        }
                        authSheet?.isCancelable = false
                        authSheet?.show(getSupportFragmentManager(), "auth sheet")
                    } else {
                        if (authSheet?.isVisible!!.not())
                            authSheet?.show(getSupportFragmentManager(), "auth sheet")
                    }
                }
            } catch (ex: Exception) {
                Log.e("MainActivity--ERROR--", "${ex.message}")
            }
            /* }, 500)*/
        }
    }
    private  fun biometricAuth(){
        fun keyGuard() {
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i = km.createConfirmDeviceCredentialIntent("Authentication required", "password")
                // startActivityForResult(i, Constants.CODE_AUTHENTICATION_VERIFICATION)

                resultLauncher.launch(i)
            } else Toast.makeText(
                this,
                "No any security setup. please setup it.",
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

                    authSheet?.dismiss()
                    MainActivity.isBioAuth = true
                    AppMain.IS_APP_IN_BACKGROUD= false
                    /* Toast.makeText(
                         applicationContext,
                         "Authentication succeeded!", Toast.LENGTH_SHORT
                     ).show()*/
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

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Blytics app")
            .setSubtitle("Log in using biometric credential")
            .setNegativeButtonText("Use pattern lock")
            .build()






    }
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            authSheet?.dismiss()
            AppMain.IS_APP_IN_BACKGROUD = false
        }
        if(result.resultCode == Activity.RESULT_CANCELED) {

        }
    }

    private fun observer() {
        bconfigViewModel.observerResponse.observe(this) {
            when (it) {
                is UserBlockResponse -> {
                    if(it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        if (it.data!!.is_block) {
                            showToast(it.message)
                            logout("User Blocked")
                        }
                    }else{
                        if(it.isUnAuth.not()){
                            showToast(it.message)
                            logout("Unauthorised login")
                        }
                    }
                    bconfigViewModel.observerResponse.value = null
                }
                is UserProfileInfoResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.let { data ->
                            saveProfileData(data)

                        }
                    } else {
                        showToast(it.message)
                    }
                    profileUpdatedLiveData.value = true
                    bconfigViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    bconfigViewModel.observerResponse.value = null

                }
            }
        }
    }

    fun logout(msg:String) {


        fun logOut(){
            pref.apply {
                HomeFragment.IS_SWITCH_SHOWN = false
                val userMobileNumber =
                    pref.getStringValue(USER_MOBILE_NUMBER, "")
                val userProfile =
                    pref.getStringValue(USER_PROFILE_IMAGE, "")
                val userName = pref.getStringValue(USER_FIRST_NAME, "")
                val deviceToken = pref.getStringValue(DEVICE_TOKEN, "")
                pref.clearPreference()
                pref.setStringValue(pref.USER_MOBILE_NUMBER, userMobileNumber)
                pref.setStringValue(pref.USER_PROFILE_IMAGE, userProfile)
                pref.setStringValue(pref.USER_FIRST_NAME, userName)
                pref.setStringValue(pref.DEVICE_TOKEN, deviceToken)
                finishAffinity()
                startActivity(Intent(this@BconfigActivity, UserActivity::class.java))
            }
        }

        val dialog = MaterialAlertDialogBuilder(this)
        dialog.apply {
            setTitle("Alert!")
            setMessage(msg)
            setCancelable(false)
            setPositiveButton(
                "Login Again"
            ) { dialog, which ->
                dialog.dismiss()
                logOut()

            }

        }
        dialog.show()

    }

    private fun observerDestination() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
           // isAllAcOpen = false
            bconfigViewModel.userBlock(UserBlockRequest(
                pref.getStringValue(SharePreferences.USER_ID, "").toInt(),
                pref.getStringValue(SharePreferences.USER_TOKEN, "")
            ))
            binding.toolbarTop.show()
            when (destination.id) {

                R.id.allAccountFragment -> {
                   // isAllAcOpen = true
                }
                R.id.contactFragment -> {
                    setToolbarTitle("Contact")
                }
                R.id.payNowFragment -> {
                    //setToolbarTitle("Send Money")
                }
                R.id.transactionHistoryFragment -> {
                    setToolbarTitle("Transaction History")
                }
                R.id.payAmountFragment -> {
                    setToolbarTitle("")
                }
                R.id.transcationStatusFragment ->{
                    binding.toolbarTop.hide()
                }


                else -> {

                }
            }
        }
    }

    private fun saveProfileData(data: UserProfileInfoResponse.Data) {
        securityQuesList.clear()
        securityQuesList.addAll(data.securityQuestion)
        data.apply {
            pref.apply {
                setStringValue(USER_ID, userId)
                setStringValue(USER_MOBILE_NUMBER, mobNo)
                setBooleanValue(USER_MOBILE_VERIFIED, mobVerified)
                setStringValue(USER_EMAIL, email)
                setBooleanValue(
                    USER_EMAIL_VERIFIED,
                    emailVerified
                )
                setStringValue(USER_ADDRESS, address)
                setStringValue(USER_COUNTRY, country)
                // setStringValue(USER_WALLET_UUID, walletUuid)
                setStringValue(USER_BVN, bvn)
                setBooleanValue(USER_BVN_VERIFIED, bvnVerified)
                setBooleanValue(USER_DOC_VERIFIED, docVerified)
                setStringValue(USER_PROFILE_STATUS, profileStatus)
                setStringValue(USER_FIRST_NAME, firstName)
                setStringValue(USER_LAST_NAME, lastName)
                setStringValue(USER_DOB, dob)
                setStringValue(USER_COUNTRY_CODE, countryCode)
                setStringValue(USER_STATE, state)
                setStringValue(USER_PIN_CODE, pincode)
                val sqlist = Gson().toJson(securityQuesList)
                setStringValue(USER_SECURITY_QUES, sqlist)


                if (avatar.updated.equals(Constants.UpdatedAvatar.AVATAR_IMAGE.name, true)) {
                    setStringValue(
                        USER_PROFILE_IMAGE,
                        avatar.avatarImage
                    )
                    //  setProfileImage(data.avatar.avatarImage)
                } else if (avatar.updated.equals(
                        Constants.UpdatedAvatar.AVATAR_DEFAULT.name,
                        true
                    )
                ) {
                    setStringValue(
                        USER_PROFILE_IMAGE,
                        avatar.defaultAvatar
                    )
                    // setProfileImage(data.avatar.defaultAvatar)
                } else {

                    val urlTextImage =
                        "https://ui-avatars.com/api/?name=" + firstName.substring(
                            0,
                            1
                        ) + lastName.substring(0, 1)
                    setStringValue(
                        USER_PROFILE_IMAGE,
                        urlTextImage
                    )
                    // setProfileImage(urlTextImage)
                }

            }
            valueChangeLiveData.value = true
        }

    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun happyThemeChanges() {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.toolbarTop.setTitleTextColor(resources.getColor(R.color.white))
                binding.toolbarTop.textAlignment= View.TEXT_ALIGNMENT_CENTER
               // binding.toolbarTop.setBackgroundResource(R.color.b_bg_color_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.toolbarTop.setTitleTextColor(resources.getColor(R.color.b_currency_blue))
                binding.toolbarTop.textAlignment= View.TEXT_ALIGNMENT_CENTER
              //  binding.toolbarTop.setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
               binding.toolbarTop.setTitleTextColor(resources.getColor(R.color.b_currency_blue ))
                binding.toolbarTop.textAlignment= View.TEXT_ALIGNMENT_CENTER
             //   binding.toolbarTop.setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }


    /* override fun onBackPressed() {
         val accType = intent.getStringExtra(Constants.ACC_TYPE)
          if(accType.equals(Constants.LINKED_ACC,true)) {

              return
          }
         if (isAllAcOpen) {
                 finish()
                 startActivity(Intent(this, MainActivity::class.java))
         } else {
             super.onBackPressed()

         }
     }*/

    //

}