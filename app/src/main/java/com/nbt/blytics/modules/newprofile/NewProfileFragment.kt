package com.nbt.blytics.modules.newprofile

import android.Manifest
import android.app.AlertDialog
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
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.profileUpdatedLiveData
import com.nbt.blytics.callback.valueChangeLiveData
import com.nbt.blytics.callback.verificationLiveData
import com.nbt.blytics.countrypicker.*
import com.nbt.blytics.countrypicker.models.CountriesStates
import com.nbt.blytics.countrypicker.models.Country
import com.nbt.blytics.databinding.AvatarBottomSheetBinding
import com.nbt.blytics.databinding.BottomSheetBvnVerifyBinding
import com.nbt.blytics.databinding.BottomSheetMobileVerifyBinding
import com.nbt.blytics.databinding.NewProfileFragmentBinding
import com.nbt.blytics.modules.bvn.models.BvnRegisterResponse
import com.nbt.blytics.modules.galleryBottomSheet.GalleryFragment
import com.nbt.blytics.modules.otp.model.EmailOtpResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.profile.UpdateMobileResponse
import com.nbt.blytics.modules.securityQes.MyViewPageAdapter
import com.nbt.blytics.modules.securityQes.Page
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.adapter.AvatarAdapter
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.userprofile.models.*
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.UtilityHelper.isEmailValid
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class NewProfileFragment : BaseFragment<NewProfileFragmentBinding, NewProfileViewModel>(), GalleryFragment.ImageSelectionListener  {
    private val TAG = "Profile Camera Permission=="
    private val profileViewModel: NewProfileViewModel by viewModels()
    private lateinit var binding: NewProfileFragmentBinding
    private lateinit var mStackLayoutManager: StackLayoutManager
    private val profileList = mutableListOf<ProfileModel>()
    private val avatarList = mutableListOf<AvatarModel.Data>()
    private lateinit var avatarBottomSheet: BottomSheetDialog
    // private lateinit var profileAdapter: ProfileAdapter
    private lateinit var emailDialog: BottomSheetDialog
    private var isCountryChanged: Boolean = false
    private var isStateChanged: Boolean = false
    private lateinit var avatarAdapter: AvatarAdapter
    private var isDoc1Selected = false
    private var isDoc2Selected = false
    private var isBVNSelected = false
    private var document1: File? = null
    private var document2: File? = null
    private var checkUserViaEamil: Boolean = false
    private var phoneVerificationId: String = ""
    private lateinit var bvnDialog: BottomSheetDialog
    private lateinit var bindingBVN: BottomSheetBvnVerifyBinding
    private var phoneToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var bottomSheetMobileVerify: BottomSheetDialog
    private var selectedImageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observerOTP()
        observer()
        setViewPager()
        showLoading()
        profileViewModel.getAvatars()
        setAvatarAdapter()
        val userID = pref().getStringValue(SharePreferences.USER_ID, "")
        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        CountryPicker(requireContext()).findCountryDataByName(SharePreferences.getStringValue(SharePreferences.USER_COUNTRY, ""))
        binding.imgInfoAddress.setOnClickListener {
            showInfoDialog("This is the address information.")
        }


        binding.imgInfoIdentity.setOnClickListener {
            showInfoDialog("A government-issued ID with a clear photo is mandatory. Acceptable documents include:\n" +
                    "Passport\n" +
                    "Driving License\n" +
                    "Any other valid Government ID")
        }

        binding.apply {
            fillData()
           /*  btnDob.setOnClickListener {
                  datePicker()
              }*/
            btnEmailSave.setOnClickListener {
                if (btnEmailSave.text.toString().equals("Verify", true)) {
                    checkUserViaEamil = true
                    showLoading()
                    profileViewModel.otpEmail(binding.edtEmail.text.toString().trim())
                } else if (edtEmail.text.toString().trim().isEmailValid()) {
                    checkUserViaEamil = true
                    showLoading()
                    profileViewModel.checkExist(edtEmail.text.toString().trim())
                } else {
                    showToast("Invalid email.")
                }
            }

            btnNameSave.setOnClickListener {
                val firstName = edtFirstName.text.toString().trim()
                val lastName = edtLastName.text.toString().trim()
                if (firstName.isNotBlank() && lastName.isNotBlank()) {
                    val pattern = Pattern.compile(Constants.SPECIAL_CHAR)
                    if (!pattern.matcher(firstName).matches() && !pattern.matcher(lastName).matches()) {
                        showLoading()
                        profileViewModel.updateProfileInfo(UpdateName(userID, userToken, firstName, lastName))
                        selectedImageUri?.let { uri ->
                            val imageUrl = uri.toString()
                            profileViewModel.updateDocument(UpdateDocument(imageUrl))
                        } ?: showToast("Please select an Document.")
                    } else {
                        showToast("Special characters are not allowed.")
                    }
                } else {
                    showToast("All fields are required.")
                }
            }

            btnDoc1.setOnClickListener {
                showDocumentDialog()
            }

            btnDoc2.setOnClickListener {
                showDocumentDialog()
            }

            btnBvnSave.setOnClickListener {
                if (edtBvn.text.toString().trim().isNotBlank()) {
                    //  if (edtBvn.text.toString().trim().length > 13) {
                    showBVNDialog()/*} else {
                        showToast("Bvn should be 14 digit.")
                    }*/
                } else {
                    showToast("Enter BVN.")
                }
            }

            btnPhoneSave.setOnClickListener {
                if (edtPhone.text.toString().trim().isNotBlank()) {
                    checkUserViaEamil = false
                    showLoading()
                    profileViewModel.checkExist(binding.edtPhone.text.toString().trim())
                } else {
                    showToast("enter phone number.")
                }
            }

            btnState.setOnClickListener {
                StatePicker(requireContext()).apply {
                    setOnStateChangeListener(object : CallbackSelectState {
                        override fun selectedState(state: CountriesStates.Country.State) {
                            isStateChanged = true
                            binding.edtState.setText(state.stateName)
                            updateDrawableLeft(binding.edtState, R.drawable.ic_cross)
                        }
                    })
                    init()
                }
            }

          /*  btnCountry.setOnClickListener {
                CountryPicker(requireContext()).apply {
                    setOnCountryChangeListener(object : CallbackCountrySelected {
                        override fun selectedCountry(country: CountriesStates.Country) {
                            isCountryChanged = true
                            binding.edtCountry.setText(country.countryName)
                            updateDrawableLeft(binding.edtCountry, R.drawable.ic_cross)
                        }
                    })
                    init()
                }
            }*/

            btnAddressSave.setOnClickListener {
                if (isCountryChanged || isStateChanged || edtAddress.text.toString().isNotBlank()) {
                    if (isCountryChanged && !isStateChanged) {
                        showToast("Please select a state after changing the country.")
                        return@setOnClickListener
                    }
                    showLoading()
                    if (isCountryChanged) {
                        profileViewModel.updateProfileInfo(UpdateCountry(userID, userToken, edtCountry.text.toString().trim(), edtState.text.toString().trim()))
                        isCountryChanged = false
                    }

                    if (isStateChanged && !isCountryChanged) {
                        profileViewModel.updateProfileInfo(UpdateCountry(userID, userToken, edtCountry.text.toString().trim(), edtState.text.toString().trim()))
                        isStateChanged = false
                    }

                    if (edtAddress.text.toString().isNotBlank()) {
                        profileViewModel.updateProfileInfo(UpdateAddress(userID, userToken, edtAddress.text.toString().trim()))
                    }
                } else {
                    showToast("Please select at least one option to update.")
                }
            }

            /*  btnCountrySave.setOnClickListener {
                  if (isCountryChanged) {
                      if (isStateChanged) {
                          isStateChanged = false
                          isCountryChanged = false
                          showLoading()
                          profileViewModel.updateProfileInfo(UpdateCountry(userID, userToken, edtCountry.text.toString().trim(), edtState.text.toString().trim()))
                      } else {
                          showToast("select state.")
                      }
                  } else {
                      showToast("select country.")
                  }
              }*/

            btnDobSave.setOnClickListener {
                showLoading()
                profileViewModel.updateProfileInfo(UpdateDob(userID, userToken, edtDob.text.toString().trim()))
            }

            btnPincodeSave.setOnClickListener {
                if (edtPincode.text.toString().trim().isBlank()) {
                    showToast("Enter pin code")
                    return@setOnClickListener
                }
                if (edtPincode.text.toString().trim().length < 5) {
                    showToast("pincode should be min 5 digit")
                    return@setOnClickListener
                }
                showLoading()
                profileViewModel.updateProfileInfo(UpdatePinCode(userID, userToken, edtPincode.text.toString().trim()))
            }

            edtCountryCode.setText(pref().getStringValue(SharePreferences.USER_COUNTRY_CODE, ""))
            btnCountryCodePicker.setOnClickListener {
                CountryCodePicker(requireContext()).apply {
                    setOnCountryChangeListener(object : CallbackSelectCountry {
                        override fun selectedCountry(country: Country) {
                            binding.edtCountryCode.setText(country.dial_code)
                        }
                    })
                    init()
                }
            }
        }

        profileUpdatedLiveData.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    fillData()
                    profileUpdatedLiveData.value = null
                }
            }
        }
    }

    fun updateDrawableLeft(editText: EditText, drawableId: Int) {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    private fun showInfoDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Information")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }.create()
            .show()
    }

    private fun datePicker() {
        val calendarEnd = Calendar.getInstance()
        calendarEnd.add(Calendar.YEAR, -10)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val endData = format.format(calendarEnd.time)
        val endYear = endData.split("-")[0]
        val endMonth = endData.split("-")[1]
        val endDay = endData.split("-")[2]
        val calendarStart = Calendar.getInstance()
        calendarStart.add(Calendar.YEAR, -100)
        val startData = format.format(calendarStart.time)
        val startYear = startData.split("-")[0]
        val startMonth = startData.split("-")[1]
        val startDay = startData.split("-")[2]
        val dialog = DatePickerFragment(
            requireActivity(),
            endDay.toInt(),
            endMonth.toInt(),
            endYear.toInt(),
            startDay.toInt(),
            startMonth.toInt(),
            startYear.toInt()
        ) { year, month, day ->
            /*     val current = Date()
             val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
             val birthDate = dateFormat.parse("$year-$month-$day")
             if (current.after(birthDate).not()) {
                 showToast(requireActivity().resources.getString(R.string.date))
             } else {*/
            binding.edtDob.setText("$year-$month-$day")
            //}
        }
        dialog.show(requireActivity().supportFragmentManager, "timePicker")
    }

    private fun fillData() {
        profileList.clear()
        binding.apply {
            val edtViewList = mutableListOf<EditText>(edtFirstName, edtLastName, edtEmail, edtBvn, edtPhone, edtAddress, edtCountry, edtState, edtPincode, edtDob)
            pref().apply {
                val isDocumentVerified = getBooleanValue(USER_DOC_VERIFIED)
                val emailLabel = if (getStringValue(USER_EMAIL, "").isBlank()) {
                    "Register your email"
                } else {
                    getStringValue(USER_EMAIL, "")
                }
                edtEmail.isFocusable = true
                edtEmail.isFocusableInTouchMode = true
                edtEmail.isClickable = true
                if (getStringValue(USER_EMAIL, "").isBlank()) {
                    btnEmailSave.text = "Save"
                } else {
                    btnEmailSave.text = "Update"
                }
                val bvnLabel = if (getStringValue(USER_BVN, "").isBlank()) {
                    "Register your BVN"
                } else {
                    getStringValue(USER_BVN, "")
                }
                if (getStringValue(USER_BVN, "").isBlank()) {
                    btnBvnSave.text = "Save"
                } else {
                    btnBvnSave.text = "Update"
                }

                for (i in edtViewList.indices) {
                    if (isDocumentVerified) {
                        edtViewList[i].setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                    } else {
                        edtViewList[i].setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross), null)
                    }
                    if (i == 4) {
                        edtViewList[i].setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                    }
                    if (i == 2) {
                        if (getBooleanValue(USER_EMAIL_VERIFIED)) {
                            edtViewList[i].setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                        } else {
                            edtViewList[i].setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross), null)
                            if (pref().getStringValue(SharePreferences.USER_EMAIL, "").isNotBlank()) {
                                if (getBooleanValue(USER_EMAIL_VERIFIED).not()) {
                                    btnEmailSave.text = "Verify"
                                    edtEmail.isFocusable = false
                                    edtEmail.isFocusableInTouchMode = false
                                    edtEmail.isClickable = false
                                } else {
                                    edtEmail.isFocusable = true
                                    edtEmail.isFocusableInTouchMode = true
                                    edtEmail.isClickable = true
                                    btnEmailSave.text = "Update"
                                }
                            }
                        }
                    }
                    if (i == 3) {
                        if (getBooleanValue(USER_BVN_VERIFIED)) {
                            edtViewList[i].setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                        } else {
                            edtViewList[i].setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross), null)
                        }
                    }
                }

                edtDob.setText(pref().getStringValue(SharePreferences.USER_DOB, ""))
                edtEmail.setText(pref().getStringValue(SharePreferences.USER_EMAIL, ""))
                edtFirstName.setText(pref().getStringValue(SharePreferences.USER_FIRST_NAME, ""))
                edtLastName.setText(pref().getStringValue(SharePreferences.USER_LAST_NAME, ""))
                val bvn = pref().getStringValue(SharePreferences.USER_BVN, "")
                if (bvn.isNotBlank()) {
                    edtBvn.setText(bvn)
                }
                edtPhone.setText(pref().getStringValue(SharePreferences.USER_MOBILE_NUMBER, ""))
                edtAddress.setText(pref().getStringValue(SharePreferences.USER_ADDRESS, ""))
                edtCountry.setText(pref().getStringValue(SharePreferences.USER_COUNTRY, ""))
                edtState.setText(pref().getStringValue(SharePreferences.USER_STATE, ""))
                edtPincode.setText(pref().getStringValue(SharePreferences.USER_PIN_CODE, ""))
                edtDob.setText(pref().getStringValue(SharePreferences.USER_DOB, ""))
            }
        }
    }

    private fun showDocumentDialog() {
        val galleryFragment = GalleryFragment.newInstance()
        galleryFragment.imageSelectionListener = this // Set listener to NewProfileFragment
        galleryFragment.show(childFragmentManager, galleryFragment.tag)
    }

    private fun showBVNDialog() {
        bvnDialog = BottomSheetDialog(requireContext()).apply {
            bindingBVN = DataBindingUtil.inflate<BottomSheetBvnVerifyBinding>(layoutInflater, R.layout.bottom_sheet_bvn_verify, null, false).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }
                edtBvn.setText(binding.edtBvn.text.toString())
                btnDoc1.setOnClickListener {
                    isDoc1Selected = true
                    isDoc2Selected = false
                    checkPermissions()
                }
                btnDoc2.setOnClickListener {
                    isDoc1Selected = false
                    isDoc2Selected = true
                    checkPermissions()
                }
                btnVerify.setOnClickListener {
                    fun validationBVN(): Boolean {
                        if (bindingBVN.edtBvn.text.toString().isBlank()) {
                            showToast("Enter BVN.")
                            return false
                        }

                        if (document1 == null) {
                            showToast("Select ID proof")
                            return false
                        }
                        if (document2 == null) {
                            showToast("Select address proof")
                            return false
                        }
                        return true
                    }

                    if (validationBVN()) {
                        binding.apply {
                            pref().apply {
                                val forBvnRegistration =
                                    pref().getStringValue(USER_BVN, "").isBlank()
                                showLoading()
                                val userId = pref().getStringValue(USER_ID, "")
                                val userToken = pref().getStringValue(USER_TOKEN, "")
                                if (userId.isNotBlank()) {
                                    profileViewModel.registerBVN(
                                        userId,
                                        bindingBVN.edtBvn.text.toString().trim(),
                                        userToken,
                                        document1!!,
                                        document2!!,
                                        forBvnRegistration
                                    )
                                }
                            }
                        }
                    } else {
                        showToast("error")
                    }
                }
            }
            show()
            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    bindingBVN.btnDoc2.setBackgroundResource(R.drawable.bg_gradient_gray_btn)
                    bindingBVN.btnDoc1.setBackgroundResource(R.drawable.bg_gradient_gray_btn)
                    bindingBVN.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                }

                Configuration.UI_MODE_NIGHT_NO -> {
                    bindingBVN.btnDoc2.setBackgroundResource(R.drawable.bg_gradient_gray_btn)
                    bindingBVN.btnDoc1.setBackgroundResource(R.drawable.bg_gradient_gray_btn)
                    bindingBVN.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)

                }

                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    bindingBVN.btnDoc2.setBackgroundResource(R.drawable.bg_gradient_gray_btn)
                    bindingBVN.btnDoc1.setBackgroundResource(R.drawable.bg_gradient_gray_btn)
                    bindingBVN.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }
            }
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

    private fun fetchFile() {
        startActivityForResult(Intent.createChooser(getFileChooserIntentForImageAndPdf(), "Select File"), Constants.GALLERY)
    }

    private fun getFileChooserIntentForImageAndPdf(): Intent {
        val mimeTypes = arrayOf("image/*", "application/pdf")
        return Intent(Intent.ACTION_GET_CONTENT).setType("image/*|application/pdf").putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Constants.CAMERA_)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.CAMERA_) {
            if (data != null) {
                val thumbnail = data.extras!!.get("data") as Bitmap
                thumbnail.let { bitMap ->
                    val imageFile = UtilityHelper.bitmapToFile(bitMap, requireContext())
                    profileViewModel.updateAvatar(pref().getStringValue(SharePreferences.USER_ID, ""), "", imageFile, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
                }
            }
        }
        if (requestCode == Constants.GALLERY) {
            if (data != null) {
                try {
                    if (isDoc1Selected) {
                        data.data?.let { documentUri ->
                            document1 = getFile(requireContext(), documentUri)
                            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    bindingBVN.btnDoc1.setBackgroundResource(R.drawable.bg_gradient_green_btn)
                                }
                                Configuration.UI_MODE_NIGHT_NO -> {
                                    bindingBVN.btnDoc1.setBackgroundResource(R.drawable.bg_gradient_green_btn)
                                }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    bindingBVN.btnDoc1.setBackgroundResource(R.drawable.bg_gradient_green_btn)
                                }
                            }
                            Log.d("File_Path", documentUri.path.toString())
                        }
                    } else if (isDoc2Selected) {
                        data.data?.let { documentUri ->
                            document2 = getFile(requireContext(), documentUri)
                            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    bindingBVN.btnDoc2.setBackgroundResource(R.drawable.bg_gradient_green_btn)
                                }
                                Configuration.UI_MODE_NIGHT_NO -> {
                                    bindingBVN.btnDoc2.setBackgroundResource(R.drawable.bg_gradient_green_btn)
                                }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    bindingBVN.btnDoc2.setBackgroundResource(R.drawable.bg_gradient_green_btn)
                                }
                            }
                        }
                    } else {
                        data.data?.let { documentUri ->
                            val file = getFile(requireContext(), documentUri)
                            val bmOptions = BitmapFactory.Options()
                            val imageBit = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
                            val imageFile = UtilityHelper.bitmapToFile(imageBit, requireContext())
                            profileViewModel.updateAvatar(pref().getStringValue(SharePreferences.USER_ID, ""), "", imageFile, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename = File(context.filesDir.path + File.separatorChar + queryName(context, uri))
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

    private fun observerOTP() {
        verificationLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is BaseActivity.CodeSentModel -> {
                    hideLoading()/*  updateCounterTextView()
                      startTimer()*/
                    showMobileVerifyBottomSheet()
                    phoneVerificationId = it.verificationId
                    phoneToken = it.token
                    verificationLiveData.value = null
                }

                is BaseActivity.VerificationCompleteModel -> {
                    hideLoading()
                    binding.apply {
                        val userID = pref().getStringValue(SharePreferences.USER_ID, "")
                        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                        bottomSheetMobileVerify.dismiss()
                        showLoading()
                        profileViewModel.updateMobile(userID, userToken, edtPhone.text.toString().trim(), pref().getStringValue(SharePreferences.DEVICE_TOKEN, ""), edtCountryCode.text.toString().trim())
                    }
                    verificationLiveData.value = null
                }

                is BaseActivity.FirebaseExceptionModel -> {
                    hideLoading()
                    if (it.e is FirebaseAuthInvalidCredentialsException) {
                        showToast("The phone number provided is incorrect")
                    }
                    Log.d("FirebaseException==", it.e.toString())
                    verificationLiveData.value = null
                }

                is BaseActivity.CodeAutoRetrievalTimeOutModel -> {
                    hideLoading()
                    showToast(it.s0)
                    Log.d("CodeAutoRetrievalT", it.s0)
                    verificationLiveData.value = null
                }
            }
        }
    }

    private fun showMobileVerifyBottomSheet() {
        var isKeyboardVisible: Boolean = false
        bottomSheetMobileVerify = BottomSheetDialog(requireContext()).apply {
            val bindingDialog = DataBindingUtil.inflate<BottomSheetMobileVerifyBinding>(layoutInflater, R.layout.bottom_sheet_mobile_verify, null, false).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnVerify.setOnClickListener {
                    fun validationOtp(): Boolean {
                        if (phoneVerificationId.isBlank()) {
                            showToast("The phone number provided is incorrect")
                            return false
                        }
                        if (otpView.otp.toString().isBlank()) {
                            showToast("Enter OTP")
                            return false
                        }
                        return true
                    }
                    if (validationOtp()) {
                        pref().apply {
                            (requireActivity() as BconfigActivity).verifyPhoneNumberWithCode(phoneVerificationId, otpView.otp.toString().trim())
                        }
                    } else {
                        showToast("error")
                    }

                }/*   otpView.setOnClickListener {
                       if (!isKeyboardVisible){
                           showSoftKeyBoard(otpView)
                           otpView.requestFocus()
                       }
                   }
                   KeyboardVisibilityEvent.setEventListener(
                       requireActivity(),
                       viewLifecycleOwner
                   ) { isOpen -> isKeyboardVisible = isOpen }*/
            }
            show()
            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }

                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }
            }
        }
    }

    private fun showEmailOTP(otp: String) {
        emailDialog = BottomSheetDialog(requireContext()).apply {
            setCancelable(false)
            val binding = DataBindingUtil.inflate<BottomSheetMobileVerifyBinding>(layoutInflater, R.layout.bottom_sheet_mobile_verify, null, false).apply {
                setContentView(root)
                lblTitle.text = "Verify Email"
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnVerify.setOnClickListener {
                    fun validationOtp(): Boolean {
                        if (otpView.otp.toString().isBlank()) {
                            showToast("Enter OTP")
                            return false
                        }
                        return true
                    }

                    if (validationOtp()) {
                        binding.apply {
                            pref().apply {
                                if (edtEmail.text.toString().trim().isEmailValid()) {
                                    if (otp.equals(otpView.otp.toString(), false)) {
                                        /* txtEmail.text = edtEmail.text.toString().trim()*/
                                        showLoading()
                                        profileViewModel.updateProfileInfo(UpdateEmail(getStringValue(USER_ID, ""), getStringValue(USER_TOKEN, ""), edtEmail.text.toString().trim(), true))
                                        dismiss()
                                    } else {
                                        showToast("invalid otp")
                                    }
                                } else {
                                    showToast("invalid email")
                                }
                            }
                        }
                    } else {
                        showToast("error")
                    }
                }
            }
            show()

            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                }

                Configuration.UI_MODE_NIGHT_NO -> {
                    binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }

                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }
            }
        }
    }

    private fun observer() {
        profileViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is UpdateMobileResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        (requireActivity() as BconfigActivity).getProfileInfo()
                    } else {
                        showToast(it.message)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is BvnRegisterResponse -> {
                    hideLoading()
                    bvnDialog.dismiss()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        binding.apply {
                            pref().apply {
                                if (edtBvn.text.toString().trim().isNotBlank()) {
                                    //txtBvn.text = edtBvn.text.toString().trim()
                                    showLoading()
                                    profileViewModel.updateProfileInfo(UpdateBVN(getStringValue(USER_ID, ""), getStringValue(USER_TOKEN, ""), edtBvn.text.toString().trim()))
                                } else {
                                    showToast("enter bvn")
                                }
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is EmailOtpResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showEmailOTP(it.message)
                    } else {
                        showToast(it.status)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is AvatarModel -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            avatarList.clear()/*  val data1 = AvatarModel.Data(
                              0, "https://www.freeiconspng.com/uploads/camera-icon-circle-21.png")
                              avatarList.add(data1)*/
                            avatarList.addAll(it.data)
                            valueChangeLiveData.value = true
                            avatarAdapter.notifyDataSetChanged()
                        }
                    } else {
                        showToast(it.status)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is UpdateAvatarResponse -> {
                    hideLoading()
                    (requireActivity() as BconfigActivity).getProfileInfo()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                        }
                    } else {
                        showToast(it.message)
                    }
                    valueChangeLiveData.value = true
                    profileViewModel.observerResponse.value = null
                }

                is UpdateProfileResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        (requireActivity() as BconfigActivity).getProfileInfo()
                        showToast(it.message)
                    } else {
                        showToast(it.message)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is CheckExistPhoneResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.FAILED.name, true)) {

                        if (checkUserViaEamil) {
                            showLoading()
                            profileViewModel.otpEmail(binding.edtEmail.text.toString().trim())
                        } else {
                            val newMobileNo = "${binding.edtCountryCode.text.toString()}${binding.edtPhone.text.toString().trim()}"
                            (requireActivity() as BconfigActivity).sendVerificationCode(newMobileNo)
                        }
                    } else {
                        if (checkUserViaEamil) {
                            showToast("Email already registered.")
                        } else {
                            showToast("Mobile number already registered.")
                        }
                    }
                    profileViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    profileViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun showAvatarDialog() {
        avatarBottomSheet.show()
    }

    private fun setAvatarAdapter() {
        avatarBottomSheet = BottomSheetDialog(requireContext()).apply {
            val avatarBottomSheetBinding = DataBindingUtil.inflate<AvatarBottomSheetBinding>(layoutInflater, R.layout.avatar_bottom_sheet, null, false).apply {
                setContentView(root)
                avatarAdapter = AvatarAdapter(avatarList) { pos ->
                    /* for (i in avatarList.indices) {
                         if (pos != i) {
                             avatarList[i].isSelect = false
                         }
                     }*/
                    //  avatarAdapter.notifyDataSetChanged()
                    /*  if (pos == 0) {
                          checkPermissions()
                      } else {*/
                    // binding.imgProfile.setImage(avatarList[pos].image)
                    avatarBottomSheet.dismiss()
                    profileViewModel.updateAvatar(pref().getStringValue(SharePreferences.USER_ID, ""), avatarList[pos].id.toString(), null, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
                    //}
                    rvAvatar.hide()
                }
                rvAvatar.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rvAvatar.adapter = avatarAdapter
                btnAvatar.setOnClickListener {
                    if (rvAvatar.isVisible) {
                        rvAvatar.hide()
                    } else {
                        rvAvatar.show()
                    }
                }

                btnPhoto.setOnClickListener {
                    checkPermissionGallery()
                }

                btnCamera.setOnClickListener {
                    checkPermissions()
                }
            }
        }
    }

    private fun checkPermissionGallery() {
        fun getFileChooserIntentForImage(): Intent {
            val mimeTypes = arrayOf("image/*")
            return Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        fun fetchFile() {
            startActivityForResult(Intent.createChooser(getFileChooserIntentForImage(), "Select Photo"), Constants.GALLERY)
        }

        if (requireContext().let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            if (requireContext().let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                fetchFile()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun checkPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (permissionsNeeded.isNotEmpty()) {
            Log.d(TAG, "Requesting Permissions: $permissionsNeeded")
            requestMultiplePermissions.launch(permissionsNeeded.toTypedArray())
        } else {
            if (permissionsNeeded.isEmpty()) {
                if (isBVNSelected) {
                    fetchFile()
                    Log.d(TAG, "Storage Permission Already Granted, Fetching File")
                } else {
                    takePhotoFromCamera()
                    Log.d(TAG, "Camera Permission Already Granted, Taking Photo")
                }
            }
        }
    }

    /*    private fun uploadingProfile(isShow: Boolean) {
            if (isShow)
                binding.layoutLoadImg.hide()
            else
                binding.layoutLoadImg.show()
        }*/

    private fun setViewPager() {
        val userProfileImageName = pref().getStringValue(SharePreferences.USER_PROFILE_IMAGE, "")
        Log.d("ImageResourceID", "Retrieved User Profile Image Name: $userProfileImageName")

        val userProfileImageResId = if (userProfileImageName.isNotEmpty()) {
            context?.resources?.getIdentifier(userProfileImageName, "drawable", requireContext().packageName) ?: 0
        } else {
            R.drawable.dummy_user
        }
        Log.d("ImageResourceID", "User Profile Image Res ID: $userProfileImageResId")

        // Define the list of pages with drawable resources and/or URLs
        val list = mutableListOf(
            Page("Name", imageUrl = userProfileImageName),
            Page("Email", drawableRes = R.drawable.mail_24dp),
            Page("Address", drawableRes = R.drawable.address_new),
            Page("DOB", drawableRes = R.drawable.calendar),
            Page("BVN", drawableRes = R.drawable.bvn),
            Page("Phone", drawableRes = R.drawable.phone_new)
        )

        val viewPagerAdapter = MyViewPageAdapter(
            requireContext(),
            list,
            -1,
            R.color.orange_light,
            R.color.white,
            R.drawable.img_samll_bg_2_squre
        )
        binding.screenViewpager.adapter = viewPagerAdapter

        // Handle night mode
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tabIndicatorNight.show()
                binding.tabIndicator.hide()
                binding.tabIndicatorNight.setupWithViewPager(binding.screenViewpager)
            }
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.tabIndicatorNight.hide()
                binding.tabIndicator.show()
                binding.tabIndicator.setupWithViewPager(binding.screenViewpager)
            }
        }

        // Page change listener to handle UI updates
        binding.screenViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                visibleStateChange(position)
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    fun visibleStateChange(i: Int) {
        binding.apply {
            when (i) {
                0 -> {
                    cardName.show()
                    cardName1.show()
                    cardEmail.hide()
                    cardAddress.hide()
                    cardAddress1.hide()
                    cardDob.hide()
                    cardBvn.hide()
                    cardPhone.hide()
                  //  cardCountry.hide()
                    cardPincode.hide()
                    btnNameSave.show()
                    btnEmailSave.hide()
                    btnAddressSave.hide()
                    btnDobSave.hide()
                    btnBvnSave.hide()
                    btnPhoneSave.hide()
                    //btnCountrySave.hide()
                    btnPincodeSave.hide()
                }

                1 -> {
                    cardName.hide()
                    cardName1.hide()
                    cardEmail.show()
                    cardAddress.hide()
                    cardAddress1.hide()
                    cardDob.hide()
                    cardBvn.hide()
                    cardPhone.hide()
                  //  cardCountry.hide()
                    cardPincode.hide()
                    btnNameSave.hide()
                    btnEmailSave.show()
                    btnAddressSave.hide()
                    btnDobSave.hide()
                    btnBvnSave.hide()
                    btnPhoneSave.hide()
                   // btnCountrySave.hide()
                    btnPincodeSave.hide()
                }

                2 -> {
                    cardName.hide()
                    cardName1.hide()
                    cardEmail.hide()
                    cardAddress.show()
                    cardAddress1.show()
                    cardDob.hide()
                    cardBvn.hide()
                    cardPhone.hide()
                  //  cardCountry.hide()
                    cardPincode.hide()
                    btnNameSave.hide()
                    btnEmailSave.hide()
                    btnAddressSave.show()
                    btnDobSave.hide()
                    btnBvnSave.hide()
                    btnPhoneSave.hide()
                  //  btnCountrySave.hide()
                    btnPincodeSave.hide()
                }

                3 -> {
                    cardName.hide()
                    cardName1.hide()
                    cardEmail.hide()
                    cardAddress.hide()
                    cardAddress1.hide()
                    cardDob.show()
                    cardBvn.hide()
                    cardPhone.hide()
                  //  cardCountry.hide()
                    cardPincode.hide()
                    btnNameSave.hide()
                    btnEmailSave.hide()
                    btnAddressSave.hide()
                    btnDobSave.hide()
                    btnBvnSave.hide()
                    btnPhoneSave.hide()
                   // btnCountrySave.hide()
                    btnPincodeSave.hide()
                }

                4 -> {
                    cardName.hide()
                    cardName1.hide()
                    cardEmail.hide()
                    cardAddress.hide()
                    cardAddress1.hide()
                    cardDob.hide()
                    cardBvn.show()
                    cardPhone.hide()
                 //   btnCountrySave.hide()
                    cardPincode.hide()
                    btnNameSave.hide()
                    btnEmailSave.hide()
                    btnAddressSave.hide()
                    btnDobSave.hide()
                    btnBvnSave.show()
                    btnPhoneSave.hide()
                  //  btnCountrySave.hide()
                    btnPincodeSave.hide()
                }

                5 -> {
                    cardName.hide()
                    cardName1.hide()
                    cardEmail.hide()
                    cardAddress.hide()
                    cardAddress1.hide()
                    cardDob.hide()
                    cardBvn.hide()
                    cardPhone.show()
                  //  cardCountry.hide()
                    cardPincode.hide()
                    btnNameSave.hide()
                    btnEmailSave.hide()
                    btnAddressSave.hide()
                    btnDobSave.hide()
                    btnBvnSave.hide()
                    btnPhoneSave.hide()
                  //  btnCountrySave.hide()
                    btnPincodeSave.hide()
                }

                6 -> {
                    cardName.hide()
                    cardName1.hide()
                    cardEmail.hide()
                    cardAddress.hide()
                    cardAddress1.hide()
                    cardDob.hide()
                    cardBvn.hide()
                    cardPhone.hide()
                  //  cardCountry.show()
                    cardPincode.hide()
                    btnNameSave.hide()
                    btnEmailSave.hide()
                    btnAddressSave.hide()
                    btnDobSave.hide()
                    btnBvnSave.hide()
                    btnPhoneSave.hide()
                  //  btnCountrySave.hide()
                    btnPincodeSave.hide()
                }

                7 -> {
                    cardName.hide()
                    cardName1.hide()
                    cardEmail.hide()
                    cardAddress.hide()
                    cardAddress1.hide()
                    cardDob.hide()
                    cardBvn.hide()
                    cardPhone.hide()
                //   cardCountry.hide()
                    cardPincode.show()
                    btnNameSave.hide()
                    btnEmailSave.hide()
                    btnAddressSave.hide()
                    btnDobSave.hide()
                    btnBvnSave.hide()
                    btnPhoneSave.hide()
                  //  btnCountrySave.hide()
                    btnPincodeSave.show()
                }
            }
        }
    }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnDobSave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnPincodeSave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
              //  binding.btnCountrySave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnAddressSave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnPhoneSave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnBvnSave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnEmailSave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnNameSave.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnDobSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnPincodeSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
             //   binding.btnCountrySave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnAddressSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnPhoneSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnBvnSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnEmailSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnNameSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnDobSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnPincodeSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
              //  binding.btnCountrySave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnAddressSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnPhoneSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnBvnSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnEmailSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnNameSave.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }
        }
    }

    override fun onImageSelected(uri: Uri) {
        selectedImageUri = uri // Store selected image URI
        showToast("Image selected: $uri") // Optional feedback
        binding.document.setColorFilter(ContextCompat.getColor(requireContext(), R.color.app_green_dark)) // Change tint color
    }

    override fun getLayoutId(): Int = R.layout.new_profile_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): NewProfileViewModel = profileViewModel
}