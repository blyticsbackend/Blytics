package com.nbt.blytics.activity.main

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nbt.blytics.AppMain
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.models.MenuItem
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.callback.notificationCallBack
import com.nbt.blytics.callback.profileUpdatedLiveData
import com.nbt.blytics.callback.valueChangeLiveData
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.ActivityMainBinding
import com.nbt.blytics.databinding.AvatarBottomSheetBinding
import com.nbt.blytics.modules.home.BalanceResponse
import com.nbt.blytics.modules.home.HomeFragment
import com.nbt.blytics.modules.payment.models.UserProfileInfoResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.adapter.AvatarAdapter
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.userprofile.models.UpdateAvatarResponse
import com.nbt.blytics.testing.AuthBottomSheetDialog
import com.nbt.blytics.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executor

class MainActivity : BaseActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var avatarBottomSheet: BottomSheetDialog
    // private var phoneNumber: String = ""
    private val avatarList = mutableListOf<AvatarModel.Data>()
    var securityQuesList = mutableListOf<UserProfileInfoResponse.Data.SQ>()
    private lateinit var navController: NavController
    private var networkDialog: AlertDialog? = null
    private lateinit var avatarAdapter: AvatarAdapter
    /*  private lateinit var mAuth: FirebaseAuth*/
    private lateinit var dialogNetworkBuilder: MaterialAlertDialogBuilder
    private var biometricPrompt: BiometricPrompt? = null
    val listPIn = mutableListOf<MenuItem>()
    private lateinit var executor: Executor
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var authSheet: AuthBottomSheetDialog? = null
    private val TAG = "Profile Camera Permission=="
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        dialogNetworkBuilder = MaterialAlertDialogBuilder(this)
        Log.d("Token***", pref.getStringValue(pref.DEVICE_TOKEN, ""))
        //toolbarBinding = mBinding.mainToolbar
        // setSupportActionBar(toolbarBinding.toolbarTop)
        // showAlertTpin()
        navController = findNavController(R.id.main_fragments_container)
        networkConnectionObserver()
        observerDestination()
        mBinding.mainToolbar.navLogout.setOnClickListener {
            showLogoutDialog()
        }

        mBinding.mainToolbar.navNotification.setOnClickListener {
            val intent = Intent(this, BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.NOTIFICATION_LIST.name)
            startActivity(intent)
        }

        setBottomNavigation()
        observer()
        getProfileInfo()

        val appMenuGson = pref.getStringValue(SharePreferences.APPS_MENU, "")
        if (appMenuGson.isNotBlank()) {
            val appMenuSaveList = Gson().fromJson<MutableList<MenuItem>>(appMenuGson, object : TypeToken<MutableList<MenuItem>>() {}.type)
            appsMenuItems.clear()
            appsMenuItems.addAll(appMenuSaveList)
        }
        val mainMenuGson = pref.getStringValue(SharePreferences.MAIN_MENU, "")
        if (mainMenuGson.isNotBlank()) {
            val mainMenuSaveList = Gson().fromJson<MutableList<MenuItem>>(mainMenuGson, object : TypeToken<MutableList<MenuItem>>() {}.type)
            menuItems.clear()
            menuItems.addAll(mainMenuSaveList)
        }

        setMenuItems()
        valueChangeLiveData.observe(this) {
            when (it) {
                true -> {
                    pref.apply {
                        mBinding.apply {
                            val imageUrl = getStringValue(USER_PROFILE_IMAGE, "")
                            setProfileImage(imageUrl)
                        }
                    }
                }
                false -> {
                    // Handle false case (if necessary)
                }
            }
        }

        notificationCallBack.observe(this) {
            when (it) {
                true -> {
                    mainViewModel.getUnReadNotification(
                        UnReadNotification(
                            pref.getStringValue(SharePreferences.USER_ID, "-1").toInt(),
                            pref.getStringValue(SharePreferences.USER_TOKEN, "")
                        ))
                    notificationCallBack.value = false
                }
                false -> {
                    // Handle false case (if necessary)
                }
            }
        }

        if (!pref.getBooleanValue(pref.IS_SECOND_TIME_OPEN_APP)) {
            setDefaultData()
            pref.setBooleanValue(pref.IS_SECOND_TIME_OPEN_APP, true)
        }
        //showLoading()
        mainViewModel.getAvatars()
        setAvatarAdapter()
        mBinding.mainToolbar.navUserProfile.setOnClickListener {
            showAvatarDialog()
        }
        happyThemeChanges()
    }
    override fun onResume() {
        super.onResume()
        if (AppMain.IS_APP_IN_BACKGROUD || AppMain.IS_COMING_FROM_SPLASH) {
            AppMain.IS_COMING_FROM_SPLASH = false/* Handler().postDelayed({*/
            try {
                if (pref.getBooleanValue(SharePreferences.SYSTEM_LOCK_IS_ACTIVE)) {
                    biometricAuth()
                    if (authSheet == null) {
                        authSheet = AuthBottomSheetDialog {
                            biometricPrompt?.authenticate(promptInfo)
                        }
                        authSheet?.isCancelable = false
                        authSheet?.show(supportFragmentManager, "auth sheet")
                    } else {
                        if (authSheet?.isVisible!!.not()) authSheet?.show(
                            supportFragmentManager, "auth sheet"
                        )
                    }
                }
            } catch (ex: Exception) {
                Log.e("MainActivity--ERROR--", "${ex.message}")
            }/* }, 500)*/
        }

        mainViewModel.getUnReadNotification(
            UnReadNotification(
                pref.getStringValue(SharePreferences.USER_ID, "-1").toInt(),
                pref.getStringValue(SharePreferences.USER_TOKEN, "")
            ))
        setProfileImage(pref.getStringValue(SharePreferences.USER_PROFILE_IMAGE, ""))
        getBalance()
    }

    fun getBalance() {
        if (pref.getStringValue(SharePreferences.USER_WALLET_UUID, "").isNotBlank()) {
            mainViewModel.getBalance(
                pref.getStringValue(SharePreferences.USER_ID, ""),
                pref.getStringValue(SharePreferences.USER_TOKEN, ""),
                pref.getStringValue(SharePreferences.USER_WALLET_UUID, ""),
            )
        }
    }

    private fun logoutSession() {
        showLoading()
        mainViewModel.logOut(
            LogoutRequest(
                pref.getStringValue(SharePreferences.USER_ID, ""),
                pref.getStringValue(SharePreferences.USER_TOKEN, ""),
                ))
    }

    private fun logoutSessionOut(msg: String) {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.apply {
            setTitle("Alert!")
            setMessage(msg)
            setCancelable(false)
            setPositiveButton("Login Again") { dialog, which ->
                dialog.dismiss()
                logout()
            }
        }
        dialog.show()
    }

    private fun logout() {
        pref.apply {
            HomeFragment.IS_SWITCH_SHOWN = false
            val userMobileNumber = pref.getStringValue(USER_MOBILE_NUMBER, "")
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
            startActivity(Intent(this@MainActivity, UserActivity::class.java))
        }
    }

    fun showLogoutDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.apply {
            setTitle("Logout")
            setMessage("Are you sure you want to logout?")
            setCancelable(false)
            setPositiveButton("Yes") { dialog, which ->
                dialog.dismiss()
                showLoading()
                mainViewModel.logOut(
                    LogoutRequest(
                        pref.getStringValue(SharePreferences.USER_ID, ""),
                        pref.getStringValue(SharePreferences.USER_TOKEN, ""),
                    ))
            }
            setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
        }
        dialog.show()
    }
    private fun showAlertTpin() {
        if (intent.hasExtra(Constants.COMING_FOR)) {
            val dialog = MaterialAlertDialogBuilder(this)
            dialog.apply {
                setTitle("Please set your tpin and security question.")
                setMessage("goto setting page.")
                setCancelable(false)
                setPositiveButton("Open") { dialog, which ->
                    dialog.dismiss()
                    navController.navigate(R.id.BAccountFragment, null)
                }
                setNegativeButton("Close") { dialog, which ->
                    dialog.dismiss()
                }
            }
            dialog.show()
        }
    }

    private val subscriptions = CompositeDisposable()
    fun setMenuItems() {
        mBinding.bottomNavigation.menu.clear()
        data class Tuple(val menuItem: MenuItem, val bitmap: Bitmap)
        val glide = Glide.with(this)
        Log.d("MenuList==", menuItems.toString())
        menuItems.forEachIndexed { index, menuItems ->
            mBinding.bottomNavigation.menu.add(Menu.NONE, menuItems.destinationId, index, menuItems.label)
        }
        subscriptions.add(Observable.fromIterable(menuItems).switchMap {
            Observable.just(Tuple(it, glide.asBitmap().load(it.image).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).submit().get()))
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            val menuItem = mBinding.bottomNavigation.menu.findItem(it.menuItem.destinationId)
            // menuItem.icon = BitmapDrawable(resources, it.bitmap)
        }, {
            // Handle errors here
        }, {
            // On complete we should setup nav controller
            mBinding.bottomNavigation.setupWithNavController(navController)
        }))

        for (i in 0 until mBinding.bottomNavigation.maxItemCount) {
            val item: android.view.MenuItem = mBinding.bottomNavigation.menu.get(i)
            item.icon = ContextCompat.getDrawable(this, menuItems[i].image)
        }

        val gsonAppsMenu = Gson().toJson(appsMenuItems)
        val gsonMainMenu = Gson().toJson(menuItems)
        pref.setStringValue(SharePreferences.APPS_MENU, gsonAppsMenu)
        pref.setStringValue(SharePreferences.MAIN_MENU, gsonMainMenu)
    }

    fun getProfileInfo() {
        pref.apply {
            showLoading()
            mainViewModel.getUserProfileInfo(getStringValue(USER_ID, ""), getStringValue(USER_TOKEN, ""))
        }
    }

    private fun observer() {
        mainViewModel.observerResponse.observe(this) {
            when (it) {
                is LogoutResponse -> {
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        logout()
                    } else {
                        showToast(it.message)
                    }
                    hideLoading()
                    mainViewModel.observerResponse.value = null
                }

                is BalanceResponse -> {
                    setCurrentBalance(Constants.DEFAULT_CURRENCY + it.data.balance)
                    mainViewModel.observerResponse.value = null
                }

                is UserBlockResponse -> {
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        if (it.data!!.is_block) {
                            showToast(it.message)
                            logoutSessionOut("User Blocked")
                        }
                    } else {
                        if (it.isUnAuth.not()) {
                            showToast(it.message)
                            logoutSessionOut("Unauthorised login")
                        }
                    }
                    mainViewModel.observerResponse.value = null
                }

                is AvatarModel -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            avatarList.clear()
                            avatarList.addAll(it.data)
                            valueChangeLiveData.value = true
                            avatarAdapter.notifyDataSetChanged()
                        }
                    } else {
                        showToast(it.status)
                    }
                    mainViewModel.observerResponse.value = null
                }

                is UpdateAvatarResponse -> {
                    hideLoading()
                    // uploadingProfile(true)
                    getProfileInfo()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            if (updated.equals(Constants.UpdatedAvatar.AVATAR_IMAGE.name, true)) {
                                mBinding.mainToolbar.navUserProfile.setImage(it.data.imageUrl)
                            } else {
                                mBinding.mainToolbar.navUserProfile.setImage(it.data.avatarDefault)
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    valueChangeLiveData.value = true
                    mainViewModel.observerResponse.value = null
                }

                is UnReadNotiRespnose -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        if (it.unread_notifications > 0) {
                            mBinding.mainToolbar.notificationCount.show()
                            if (it.unread_notifications > 9) {
                                mBinding.mainToolbar.notificationCount.text = "9+"
                            } else {
                                mBinding.mainToolbar.notificationCount.text = it.unread_notifications.toString()
                            }
                        } else {
                            mBinding.mainToolbar.notificationCount.hide()
                        }
                    }
                    mainViewModel.observerResponse.value = null
                }

                is UserProfileInfoResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.let { data ->
                            saveProfileData(data)
                        }
                    } else {
                        if (it.errorCode == "106") {
                            logoutSessionOut("Unauthorised login")
                        }
                        showToast(it.message)
                    }
                    profileUpdatedLiveData.value = true
                    mainViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    mainViewModel.observerResponse.value = null

                }
                is Throwable -> {
                    hideLoading()
                    mainViewModel.baseObserverResponse.value = null
                }
            }
        }
    }

    private fun observerDestination() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            /*  when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                  Configuration.UI_MODE_NIGHT_YES -> {
                  setToolbarBackGround(R.color.black)
                  }
                  Configuration.UI_MODE_NIGHT_NO -> {
                      setToolbarBackGround(R.color.white)
                  }
                  Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                      setToolbarBackGround(R.color.white)
                  }
              }*/

            mainViewModel.userBlock(
                UserBlockRequest(
                    pref.getStringValue(SharePreferences.USER_ID, "").toInt(),
                    pref.getStringValue(SharePreferences.USER_TOKEN, "")
                ))
            when (destination.id) {
                /*R.id.home -> {
                }*/
                R.id.profileFragment -> {
                    setToolbar(View.GONE)
                }
                R.id.transactionFragment -> {
                    setToolbar(View.GONE)
                }
                else -> {
                    setToolbar(View.VISIBLE)
                }
            }
        }
    }

    private fun setToolbar(status: Int) {
        mBinding.mainToolbar.toolbarTop.visibility = status
    }

    fun setToolbarBackGround(res: Int) {
        mBinding.mainToolbar.toolbarTop.setBackgroundResource(res)
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
                setBooleanValue(USER_EMAIL_VERIFIED, emailVerified)
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
                    setStringValue(USER_PROFILE_IMAGE, avatar.avatarImage)
                    setProfileImage(data.avatar.avatarImage)
                } else if (avatar.updated.equals(Constants.UpdatedAvatar.AVATAR_DEFAULT.name, true)) {
                    setStringValue(USER_PROFILE_IMAGE, avatar.defaultAvatar)
                    setProfileImage(data.avatar.defaultAvatar)
                } else {
                    val urlTextImage =/*"https://img.buymeacoffee.com/api/?name=John+Doe"*//*   "https://avatar.oxro.io/avatar.png?name=" + firstName.substring( 0, 1 ) + lastName.substring(0, 1) + "&background=0070C0&length=2"*/
                        "https://ui-avatars.com/api/?name=" + firstName.substring(0, 1) + lastName.substring(0, 1)
                    setStringValue(USER_PROFILE_IMAGE, urlTextImage)
                    setProfileImage(urlTextImage)
                }
            }
            valueChangeLiveData.value = true
        }
    }

    fun setProfileImage(img: String) {
        mBinding.mainToolbar.navUserProfile.setImage(img)
        mBinding.mainToolbar.txtUserCountry.text = pref.getStringValue(SharePreferences.USER_COUNTRY, "")
        mBinding.mainToolbar.txtUserName.text = "${pref.getStringValue(SharePreferences.USER_FIRST_NAME, "")} ${pref.getStringValue(SharePreferences.USER_LAST_NAME, "")}"
    }
    fun setCurrentBalance(str: String) {
        mBinding.mainToolbar.txtBalance.text = str
    }

    private fun setBottomNavigation() {
        mBinding.bottomNavigation.setupWithNavController(navController)
        mBinding.bottomNavigation.setOnNavigationItemReselectedListener { item ->
            when (item.itemId) {
                R.id.userProfileFragment -> {
                }
                else -> {
                }/* R.id.nav_transaction -> {
                     showToast("coming soon")
                 }*/
            }
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
        if (networkDialog == null) networkDialog = dialogNetworkBuilder.show()
    }

    private fun biometricAuth() {
        fun keyGuard() {
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i = km.createConfirmDeviceCredentialIntent("Authentication required", "password")
                // startActivityForResult(i, Constants.CODE_AUTHENTICATION_VERIFICATION)
                resultLauncher.launch(i)
            } else {
                Toast.makeText(this, "No any security setup. please setup it.", Toast.LENGTH_SHORT).show()
            }
        }

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isBioAuth = false/*  Toast.makeText(applicationContext,"Authentication error: $errString", Toast.LENGTH_SHORT).show()*/
                    keyGuard()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    authSheet?.dismiss()
                    isBioAuth = true
                    AppMain.IS_APP_IN_BACKGROUD = false/* Toast.makeText(applicationContext,"Authentication succeeded!", Toast.LENGTH_SHORT).show()*/
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isBioAuth = false
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Unlock Blytics app")
            .setSubtitle("Log in using biometric credential")
            .setNegativeButtonText("Use pattern lock").build()
    }

    companion object {
        var isModeChange = false
        var isBioAuth = false
        val appsMenuItems = mutableListOf(
           /* MenuItem(
                id = 1,
                label = "Card",
                image = R.drawable.icon_menu_card,
                destinationId = R.id.cardFragment,
                count = 0,
                name = MenuItem.Name.CARD
            ), MenuItem(
                id = 2,
                label = "Loan",
                image = R.drawable.icon_menu_loan,
                destinationId = R.id.loanFragment,
                count = 0,
                name = MenuItem.Name.LONE
            ),*/
            MenuItem(
                id = 1,
                label = "Accounts",
                image = R.drawable.ac_details,
                destinationId = R.id.allAcInfoFragment,
                count = 0,
                name = MenuItem.Name.ACCOUNT
            ))

        val menuItems = mutableListOf(
            MenuItem(
                id = 0,
                label = "Transaction",
                image = R.drawable.icon_transaction_menu,
                destinationId = R.id.homeFragment,
                count = 1,
                name = MenuItem.Name.TRANSACTION
            ),
            MenuItem(
                id = 1,
                label = "Setting",
                image = R.drawable.icon_menu_setting,
                destinationId = R.id.BAccountFragment,
                count = 1,
                name = MenuItem.Name.SETTING
            ),
            MenuItem(
                id = 2,
                label = "Home",
                image = R.drawable.icon_menu_home,
                destinationId = R.id.transactionFragment,
                count = 0,
                name = MenuItem.Name.OTHER
            ),
            MenuItem(
                id = 3,
                label = "Payee",
                image = R.drawable.nigerian,
                destinationId = R.id.payeeFragment,
                count = 1,
                name = MenuItem.Name.PAYEE
            ),
            MenuItem(
                id = 4,
                label = "Apps",
                image = R.drawable.icon_menu_apps,
                destinationId = R.id.menuFragment,
                count = 0,
                name = MenuItem.Name.APPS),
            )
    }

    var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authSheet?.dismiss()
            AppMain.IS_APP_IN_BACKGROUD = false
        }
        if (result.resultCode == Activity.RESULT_CANCELED) {
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.CAMERA] == true) {
                Log.d(TAG, "Permission granted")
                // avatarBottomSheet.dismiss()
                takePhotoFromCamera()

            } else {
                Log.d(TAG, "Permission not granted")
            }
        }

    private fun showAvatarDialog() {
        avatarBottomSheet.show()
    }

    private fun setAvatarAdapter() {
        avatarBottomSheet = BottomSheetDialog(this).apply {
            val avatarBottomSheetBinding = DataBindingUtil.inflate<AvatarBottomSheetBinding>(layoutInflater, R.layout.avatar_bottom_sheet, null, false).apply {
                setContentView(root)
                avatarAdapter = AvatarAdapter(avatarList) { pos ->
                    mBinding.mainToolbar.navUserProfile.setImage(avatarList[pos].image)
                    avatarBottomSheet.dismiss()
                    mainViewModel.updateAvatar(
                        pref.getStringValue(SharePreferences.USER_ID, ""), avatarList[pos].id.toString(), null,
                        pref.getStringValue(SharePreferences.USER_TOKEN, ""))
                    // uploadingProfile(false)
                    rvAvatar.hide()
                }
                rvAvatar.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                rvAvatar.adapter = avatarAdapter
                btnAvatar.setOnClickListener {
                    if (rvAvatar.isVisible) {
                        rvAvatar.hide()
                    } else {
                        rvAvatar.show()
                    }
                }
                btnPhoto.setOnClickListener {
                    checkPermissionGallary()
                }
                btnCamera.setOnClickListener {
                    checkPermissions()
                }
            }
        }
    }

    private fun checkPermissionGallary() {
        fun getFileChooserIntentForImage(): Intent {
            val mimeTypes = arrayOf("image/*")
            return Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }

        fun fetchFile() {
            avatarBottomSheet.dismiss()
            onResultGallery.launch(Intent.createChooser(getFileChooserIntentForImage(), "Select Photo"))
        }

        if (this.let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            if (this.let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) { requestMultiplePermissions.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                fetchFile()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun checkPermissions() {
        if (this.let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
            } != PackageManager.PERMISSION_GRANTED) {
            // Log.d(TAG, "Request Permissions")
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA))
        } else {
            if (this.let {
                    ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
                } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                avatarBottomSheet.dismiss()
                takePhotoFromCamera()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //startActivityForResult(intent, Constants.CAMERA_)
        onResultCamera.launch(intent)
    }

    private var onResultCamera = registerForActivityResult(StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK) {
            if (data != null) {
                val thumbnail = data.extras!!.get("data") as Bitmap
                thumbnail.let { bitMap ->
                    mBinding.mainToolbar.navUserProfile.setImage(bitMap)
                    val imageFile = UtilityHelper.bitmapToFile(bitMap, this)
                    mainViewModel.updateAvatar(
                        pref.getStringValue(SharePreferences.USER_ID, ""), "", imageFile,
                        pref.getStringValue(SharePreferences.USER_TOKEN, ""))
                    //uploadingProfile(false)
                }
            }
        }
    }
    private var onResultGallery = registerForActivityResult(StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    data.data?.let { documentUri ->
                        val file = getFile(this, documentUri)
                        val bmOptions = BitmapFactory.Options()
                        val imageBit = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
                        mBinding.mainToolbar.navUserProfile.setImage(imageBit)
                        val imageFile = UtilityHelper.bitmapToFile(imageBit, this)
                        mainViewModel.updateAvatar(
                            pref.getStringValue(SharePreferences.USER_ID, ""), "", imageFile,
                            pref.getStringValue(SharePreferences.USER_TOKEN, ""))
                        // uploadingProfile(false)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename =
            File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { ins ->
                createFileFromStream(ins!!, destinationFilename)
            }
        } catch (e: Exception) {
            Log.e("Save File", e.message!!)
            e.printStackTrace()
        }
        return destinationFilename
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: java.lang.Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        Log.d("File_Name=======", name)
        return name
    }

    private fun setDefaultData() {
        pref.setBooleanValue(SharePreferences.SYSTEM_LOCK_IS_ACTIVE, true)
        pref.setBooleanValue(SharePreferences.PASSWORD_LOCK_IS_ACTIVE, false)
        pref.setBooleanValue(SharePreferences.SMS_NOTIFICATION, true)
        pref.setBooleanValue(SharePreferences.WHATSAPP_NOTIFICATION, false)
        pref.setBooleanValue(SharePreferences.EMAIL_NOTIFICATION, false)
        pref.setBooleanValue(SharePreferences.SYSTEM_MODE_DAY, true)
        pref.setBooleanValue(SharePreferences.SYSTEM_MODE_NIGHT, false)
    }

    private fun happyThemeChanges() {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mBinding.mainToolbar.txtBalance.setTextColor(resources.getColor(R.color.white))
                mBinding.mainToolbar.toolbarTop.setBackgroundResource(R.color.b_bg_color_dark)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                mBinding.mainToolbar.toolbarTop.setBackgroundResource(R.color.b_bg_color_light)
                mBinding.mainToolbar.txtBalance.setTextColor(resources.getColor(R.color.b_blue_600))
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                mBinding.mainToolbar.toolbarTop.setBackgroundResource(R.color.b_bg_color_light)
                mBinding.mainToolbar.txtBalance.setTextColor(resources.getColor(R.color.b_blue_600))
            }
        }
    }
}